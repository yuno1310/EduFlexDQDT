package com.eduflex.android.ui.quiz;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.eduflex.android.model.SubmitFillBlankRequest;
import com.eduflex.android.model.SubmitFillBlankResponse;

import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FillBlankQuizMockFragment extends Fragment {

    private QuizApi quizApi;
    private TokenManager tokenManager;

    private String lessonId;
    private String courseId;
    private String lessonTitle;
    private long questionId;

    private TextView tvTitle;
    private TextView tvQuestion;
    private EditText etAnswer;
    private Button btnSubmit;

    public FillBlankQuizMockFragment() {
        super(R.layout.fragment_fill_blank_quiz_mock);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        quizApi = ApiClient.createAuthenticatedService(QuizApi.class);
        tokenManager = new TokenManager(requireContext());

        Bundle args = getArguments();
        lessonId = args != null ? args.getString("lessonId", "") : "";
        courseId = args != null ? args.getString("courseId", "") : "";
        lessonTitle = args != null ? args.getString("lessonTitle", "Quiz điền từ") : "Quiz điền từ";

        tvTitle = view.findViewById(R.id.tv_fill_blank_quiz_title);
        tvQuestion = view.findViewById(R.id.tv_fill_blank_quiz_question);
        etAnswer = view.findViewById(R.id.et_fill_blank_answer);
        btnSubmit = view.findViewById(R.id.btn_submit_fill_blank_quiz);

        tvTitle.setText(lessonTitle + " - Điền từ");

        view.findViewById(R.id.btn_back_fill_blank_quiz)
                .setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        btnSubmit.setOnClickListener(v -> submitQuiz());

        if (lessonId == null || lessonId.isEmpty()) {
            tvQuestion.setText("Quiz không khả dụng cho bài học này.");
            btnSubmit.setEnabled(false);
            return;
        }

        loadQuiz();
    }

    private void loadQuiz() {
        tvQuestion.setText("Đang tải câu hỏi...");
        btnSubmit.setEnabled(false);

        quizApi.getQuiz(lessonId).enqueue(new Callback<QuizGetResponse>() {
            @Override
            public void onResponse(@NonNull Call<QuizGetResponse> call,
                                   @NonNull Response<QuizGetResponse> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    QuizGetResponse quiz = response.body();
                    questionId = quiz.getQuestionId();
                    String text = quiz.getQuestionText();
                    if (text == null || text.trim().isEmpty()) {
                        tvQuestion.setText("Không tìm thấy câu hỏi cho bài học này.");
                    } else {
                        tvQuestion.setText(text);
                        btnSubmit.setEnabled(true);
                    }
                } else {
                    String msg = "Không thể tải câu hỏi.";
                    if (response.body() != null && response.body().getMessage() != null
                            && !response.body().getMessage().trim().isEmpty()) {
                        msg = response.body().getMessage();
                    }
                    tvQuestion.setText(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<QuizGetResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                tvQuestion.setText("Lỗi mạng khi tải câu hỏi.");
            }
        });
    }

    private void submitQuiz() {
        String input = etAnswer.getText() == null ? "" : etAnswer.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(requireContext(), "Nhập câu trả lời trước khi nộp.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = tokenManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập lại để nộp bài.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);

        SubmitFillBlankRequest.BlankAnswer answer = new SubmitFillBlankRequest.BlankAnswer(questionId, input);
        SubmitFillBlankRequest request = new SubmitFillBlankRequest(
                userId, lessonId, Collections.singletonList(answer));

        quizApi.submitFillBlank(request).enqueue(new Callback<SubmitFillBlankResponse>() {
            @Override
            public void onResponse(@NonNull Call<SubmitFillBlankResponse> call,
                                   @NonNull Response<SubmitFillBlankResponse> response) {
                if (!isAdded()) return;
                btnSubmit.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    navigateToResult(response.body());
                } else {
                    Toast.makeText(requireContext(), "Nộp bài thất bại.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SubmitFillBlankResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                btnSubmit.setEnabled(true);
                Toast.makeText(requireContext(), "Lỗi mạng khi nộp bài.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToResult(SubmitFillBlankResponse result) {
        boolean passed = result.getCorrectCount() > 0;
        float scorePercent = result.getTotalQuestions() > 0
                ? (result.getCorrectCount() * 100f) / result.getTotalQuestions()
                : 0f;
        int xpRewarded = passed ? 30 : 0;

        String message = result.getMessage() != null ? result.getMessage()
                : (passed ? "Bạn đã trả lời đúng!" : "Câu trả lời chưa đúng.");

        Bundle args = new Bundle();
        args.putString("lessonTitle", lessonTitle);
        args.putString("message", message);
        args.putInt("correctCount", result.getCorrectCount());
        args.putInt("totalQuestions", result.getTotalQuestions());
        args.putFloat("scorePercent", scorePercent);
        args.putInt("xpRewarded", xpRewarded);
        args.putBoolean("passed", passed);

        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.quizResultFragment, args);
    }
}
