package com.eduflex.android.ui.quiz;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.eduflex.android.R;
import com.eduflex.android.api.ApiClient;
import com.eduflex.android.api.QuizApi;
import com.eduflex.android.auth.TokenManager;
import com.eduflex.android.model.QuizGetResponse;
import com.eduflex.android.model.SubmitQuizRequest;
import com.eduflex.android.model.SubmitQuizResponse;

import java.util.ArrayList;
import java.util.Locale;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizFragment extends Fragment {

    private QuizApi quizApi;
    private TokenManager tokenManager;
    private String lessonId;
    private long questionId;
    private List<QuizGetResponse.OptionResponse> options = new ArrayList<>();

    private TextView tvQuizTitle;
    private TextView tvQuizQuestion;
    private RadioGroup rgOptions;
    private RadioButton rbOption1;
    private RadioButton rbOption2;
    private RadioButton rbOption3;
    private RadioButton rbOption4;
    private Button btnSubmit;
    private TextView tvResult;

    public QuizFragment() {
        super(R.layout.fragment_quiz);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        quizApi = ApiClient.createAuthenticatedService(QuizApi.class);
        tokenManager = new TokenManager(requireContext());

        Bundle args = getArguments();
        lessonId = args != null ? args.getString("lessonId", "") : "";
        String lessonTitle = args != null ? args.getString("lessonTitle", "Quiz") : "Quiz";

        tvQuizTitle = view.findViewById(R.id.tv_quiz_title);
        tvQuizQuestion = view.findViewById(R.id.tv_quiz_question);
        rgOptions = view.findViewById(R.id.rg_quiz_options);
        rbOption1 = view.findViewById(R.id.rb_option_1);
        rbOption2 = view.findViewById(R.id.rb_option_2);
        rbOption3 = view.findViewById(R.id.rb_option_3);
        rbOption4 = view.findViewById(R.id.rb_option_4);
        btnSubmit = view.findViewById(R.id.btn_submit_quiz);
        tvResult = view.findViewById(R.id.tv_quiz_result);
        tvResult.setText("");

        tvQuizTitle.setText(lessonTitle + " - Quiz");

        view.findViewById(R.id.btn_back_quiz).setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.popBackStack();
        });

        if (lessonId == null || lessonId.isEmpty()) {
            tvQuizQuestion.setText("Quiz is unavailable for this lesson.");
            btnSubmit.setEnabled(false);
            return;
        }

        loadQuiz();

        btnSubmit.setOnClickListener(v -> {
            int selectedId = rgOptions.getCheckedRadioButtonId();
            if (selectedId == -1) {
                tvResult.setText("Please choose one answer before submitting.");
                return;
            }

            long selectedOptionId = getSelectedOptionId(selectedId);
            if (selectedOptionId <= 0 || questionId <= 0) {
                tvResult.setText("Quiz data is invalid. Please reload.");
                return;
            }

            submitQuiz(selectedOptionId);
        });
    }

    private void loadQuiz() {
        tvQuizQuestion.setText("Loading quiz...");
        btnSubmit.setEnabled(false);

        quizApi.getQuiz(lessonId).enqueue(new Callback<QuizGetResponse>() {
            @Override
            public void onResponse(@NonNull Call<QuizGetResponse> call, @NonNull Response<QuizGetResponse> response) {
                if (!isAdded()) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    QuizGetResponse quiz = response.body();
                    questionId = quiz.getQuestionId();
                    options = quiz.getOptions() != null ? quiz.getOptions() : new ArrayList<>();
                    String questionText = quiz.getQuestionText();
                    if (questionText == null || questionText.trim().isEmpty()) {
                        tvQuizQuestion.setText("No quiz question found for this lesson.");
                        btnSubmit.setEnabled(false);
                        bindOptions(new ArrayList<>());
                    } else {
                        tvQuizQuestion.setText(questionText);
                        bindOptions(options);
                    }
                } else {
                    String message = "Failed to load quiz.";
                    if (response.body() != null && response.body().getMessage() != null
                        && !response.body().getMessage().trim().isEmpty()) {
                        message = response.body().getMessage();
                    }
                    tvQuizQuestion.setText(message);
                    btnSubmit.setEnabled(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<QuizGetResponse> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    return;
                }
                tvQuizQuestion.setText("Network error while loading quiz.");
                btnSubmit.setEnabled(false);
            }
        });
    }

    private void bindOptions(List<QuizGetResponse.OptionResponse> optionList) {
        RadioButton[] radioButtons = { rbOption1, rbOption2, rbOption3, rbOption4 };
        for (int i = 0; i < radioButtons.length; i++) {
            if (i < optionList.size()) {
                radioButtons[i].setVisibility(View.VISIBLE);
                radioButtons[i].setText(optionList.get(i).getOptionText());
            } else {
                radioButtons[i].setVisibility(View.GONE);
            }
        }
        rgOptions.clearCheck();
        btnSubmit.setEnabled(!optionList.isEmpty());
    }

    private long getSelectedOptionId(int selectedRadioId) {
        int index;
        if (selectedRadioId == R.id.rb_option_1) {
            index = 0;
        } else if (selectedRadioId == R.id.rb_option_2) {
            index = 1;
        } else if (selectedRadioId == R.id.rb_option_3) {
            index = 2;
        } else if (selectedRadioId == R.id.rb_option_4) {
            index = 3;
        } else {
            return -1;
        }

        if (index < 0 || index >= options.size()) {
            return -1;
        }
        return options.get(index).getOptionId();
    }

    private void submitQuiz(long selectedOptionId) {
        String userId = tokenManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            tvResult.setText("Please login again to submit quiz.");
            return;
        }

        List<SubmitQuizRequest.AnswerItem> answers = new ArrayList<>();
        answers.add(new SubmitQuizRequest.AnswerItem(questionId, selectedOptionId));
        SubmitQuizRequest request = new SubmitQuizRequest(userId, lessonId, answers);

        btnSubmit.setEnabled(false);
        quizApi.submitQuiz(request).enqueue(new Callback<SubmitQuizResponse>() {
            @Override
            public void onResponse(@NonNull Call<SubmitQuizResponse> call, @NonNull Response<SubmitQuizResponse> response) {
                if (!isAdded()) {
                    return;
                }
                btnSubmit.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    SubmitQuizResponse result = response.body();
                    String message = result.getMessage() == null ? "Quiz submitted." : result.getMessage();
                    String details = String.format(
                        Locale.US,
                        "%s\nScore: %.0f%% (%d/%d)\nXP rewarded: %d",
                        message,
                        result.getScorePercent(),
                        result.getCorrectCount(),
                        result.getTotalQuestions(),
                        result.getXpRewarded()
                    );
                    tvResult.setText(details);
                } else {
                    tvResult.setText("Failed to submit quiz.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<SubmitQuizResponse> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    return;
                }
                btnSubmit.setEnabled(true);
                tvResult.setText("Network error while submitting quiz.");
                Toast.makeText(requireContext(), "Submit failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
