package com.eduflex.android.ui.quiz;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.eduflex.android.model.Lesson;
import com.eduflex.android.model.QuizGetResponse;
import com.eduflex.android.model.SubmitQuizRequest;
import com.eduflex.android.model.SubmitQuizResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizFragment extends Fragment {

    private static final String PREF_COURSE_PROGRESS = "course_progress";

    private QuizApi quizApi;
    private TokenManager tokenManager;
    private String lessonId;
    private String courseId;
    private long questionId;
    private List<QuizGetResponse.OptionResponse> options = new ArrayList<>();
    private int lessonIndex;
    private List<Lesson> lessonList = new ArrayList<>();
    private Bundle originalArgs;

    private TextView tvQuizTitle;
    private TextView tvQuizQuestion;
    private RadioGroup rgOptions;
    private RadioButton rbOption1;
    private RadioButton rbOption2;
    private RadioButton rbOption3;
    private RadioButton rbOption4;
    private Button btnSubmit;

    public QuizFragment() {
        super(R.layout.fragment_quiz);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        quizApi = ApiClient.createAuthenticatedService(QuizApi.class);
        tokenManager = new TokenManager(requireContext());

        Bundle args = getArguments();
        originalArgs = args;
        lessonId = args != null ? args.getString("lessonId", "") : "";
        courseId = args != null ? args.getString("courseId", "") : "";
        String lessonTitle = args != null ? args.getString("lessonTitle", "Quiz") : "Quiz";
        lessonIndex = args != null ? args.getInt("lessonIndex", 0) : 0;
        lessonList = getLessonList(args);

        tvQuizTitle = view.findViewById(R.id.tv_quiz_title);
        tvQuizQuestion = view.findViewById(R.id.tv_quiz_question);
        rgOptions = view.findViewById(R.id.rg_quiz_options);
        rbOption1 = view.findViewById(R.id.rb_option_1);
        rbOption2 = view.findViewById(R.id.rb_option_2);
        rbOption3 = view.findViewById(R.id.rb_option_3);
        rbOption4 = view.findViewById(R.id.rb_option_4);
        btnSubmit = view.findViewById(R.id.btn_submit_quiz);
        Button btnPrev = view.findViewById(R.id.btn_prev_lesson);
        Button btnNext = view.findViewById(R.id.btn_next_lesson);

        tvQuizTitle.setText(lessonTitle + " - Quiz");

        view.findViewById(R.id.btn_back_quiz).setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.popBackStack();
        });

        btnPrev.setEnabled(lessonIndex > 0);
        btnNext.setEnabled(lessonIndex < lessonList.size() - 1);
        btnPrev.setOnClickListener(v -> navigateToLesson(lessonIndex - 1));
        btnNext.setOnClickListener(v -> navigateToLesson(lessonIndex + 1));

        if (lessonId == null || lessonId.isEmpty()) {
            tvQuizQuestion.setText("Quiz is unavailable for this lesson.");
            btnSubmit.setEnabled(false);
            return;
        }

        loadQuiz();

        btnSubmit.setOnClickListener(v -> {
            int selectedId = rgOptions.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(requireContext(), "Please choose one answer before submitting.", Toast.LENGTH_SHORT).show();
                return;
            }

            long selectedOptionId = getSelectedOptionId(selectedId);
            if (selectedOptionId <= 0 || questionId <= 0) {
                Toast.makeText(requireContext(), "Quiz data is invalid. Please reload.", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(requireContext(), "Please login again to submit quiz.", Toast.LENGTH_SHORT).show();
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
                    saveCourseProgress(result.getCourseProgress());
                    navigateToResultScreen(result);
                } else {
                    Toast.makeText(requireContext(), "Failed to submit quiz.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SubmitQuizResponse> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    return;
                }
                btnSubmit.setEnabled(true);
                Toast.makeText(requireContext(), "Submit failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveCourseProgress(Double courseProgress) {
        if (courseProgress == null || courseId == null || courseId.isEmpty() || !isAdded()) {
            return;
        }

        int progress = (int) Math.round(courseProgress);
        progress = Math.max(0, Math.min(100, progress));

        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_COURSE_PROGRESS, Context.MODE_PRIVATE);
        prefs.edit().putInt(courseId, progress).apply();
    }

    private void navigateToResultScreen(SubmitQuizResponse result) {
        if (!isAdded()) {
            return;
        }

        String message = result.getMessage() == null ? "Quiz submitted." : result.getMessage();
        String lessonTitle = tvQuizTitle.getText().toString().replace(" - Quiz", "");

        QuizResultDialog.newInstance(
                lessonTitle,
                result.isPassed(),
                (float) result.getScorePercent(),
                result.getCorrectCount(),
                result.getTotalQuestions(),
                result.getXpRewarded(),
                message
        ).show(getParentFragmentManager(), "quiz_result");
    }

    @SuppressWarnings("unchecked")
    private List<Lesson> getLessonList(Bundle args) {
        if (args == null) return new ArrayList<>();
        Object raw = args.getSerializable("lessonList");
        if (raw instanceof ArrayList) return (ArrayList<Lesson>) raw;
        return new ArrayList<>();
    }

    private void navigateToLesson(int index) {
        if (index < 0 || index >= lessonList.size() || !isAdded()) return;
        Lesson lesson = lessonList.get(index);

        String type = lesson.getContentType() == null ? "" : lesson.getContentType().toLowerCase();
        String courseId = originalArgs != null ? originalArgs.getString("courseId", "") : "";
        int sourceTab = originalArgs != null ? originalArgs.getInt("sourceTab", 0) : 0;

        Bundle args = new Bundle();
        args.putString("lessonId", lesson.getLessonID());
        args.putString("lessonTitle", lesson.getTitle());
        args.putString("courseId", courseId);
        args.putString("contentType", lesson.getContentType());
        args.putInt("sourceTab", sourceTab);
        args.putInt("lessonIndex", index);
        args.putSerializable("lessonList", new ArrayList<>(lessonList));

        NavController nav = NavHostFragment.findNavController(this);
        if ("quiz_fill_blank".equals(type) || "quiz_dien_tu".equals(type)
                || "quiz_new".equals(type) || "quiz_new_type".equals(type)) {
            nav.navigate(R.id.fillBlankQuizFragment, args);
        } else if ("quiz".equals(type)) {
            nav.navigate(R.id.quizFragment, args);
        } else {
            args.putString("lessonContent", getMockContent(lesson.getTitle(), type));
            nav.navigate(R.id.lessonStudyFragment, args);
        }
    }

    private String getMockContent(String title, String type) {
        if ("video".equals(type)) return "VIDEO_PLACEHOLDER";
        return "This is lesson content for: " + title
                + "\n\nIn this lesson, you will learn key concepts and practical examples."
                + "\n\n- Topic overview\n- Main ideas\n- Practical notes";
    }
}
