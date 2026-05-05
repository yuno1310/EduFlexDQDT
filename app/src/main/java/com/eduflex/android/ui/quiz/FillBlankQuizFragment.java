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
import androidx.navigation.fragment.NavHostFragment;

import com.eduflex.android.R;
import com.eduflex.android.api.ApiClient;
import com.eduflex.android.api.QuizApi;
import com.eduflex.android.auth.TokenManager;
import com.eduflex.android.model.Lesson;
import com.eduflex.android.model.QuizGetResponse;
import com.eduflex.android.model.SubmitFillBlankRequest;
import com.eduflex.android.model.SubmitFillBlankResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FillBlankQuizFragment extends Fragment {

    private QuizApi quizApi;
    private TokenManager tokenManager;

    private String lessonId;
    private String courseId;
    private String lessonTitle;
    private long questionId;
    private int lessonIndex;
    private List<Lesson> lessonList = new ArrayList<>();
    private Bundle originalArgs;

    private TextView tvTitle;
    private TextView tvQuestion;
    private EditText etAnswer;
    private Button btnSubmit;

    public FillBlankQuizFragment() {
        super(R.layout.fragment_fill_blank_quiz);
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
        lessonTitle = args != null ? args.getString("lessonTitle", "Quiz điền từ") : "Quiz điền từ";
        lessonIndex = args != null ? args.getInt("lessonIndex", 0) : 0;
        lessonList = getLessonList(args);

        tvTitle = view.findViewById(R.id.tv_fill_blank_quiz_title);
        tvQuestion = view.findViewById(R.id.tv_fill_blank_quiz_question);
        etAnswer = view.findViewById(R.id.et_fill_blank_answer);
        btnSubmit = view.findViewById(R.id.btn_submit_fill_blank_quiz);
        Button btnPrev = view.findViewById(R.id.btn_prev_lesson);
        Button btnNext = view.findViewById(R.id.btn_next_lesson);

        tvTitle.setText(lessonTitle + " - Điền từ");

        view.findViewById(R.id.btn_back_fill_blank_quiz)
                .setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        btnPrev.setEnabled(lessonIndex > 0);
        btnNext.setEnabled(lessonIndex < lessonList.size() - 1);
        btnPrev.setOnClickListener(v -> navigateToLesson(lessonIndex - 1));
        btnNext.setOnClickListener(v -> navigateToLesson(lessonIndex + 1));

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

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()
                        && response.body().getQuestions() != null && !response.body().getQuestions().isEmpty()) {
                    QuizGetResponse.QuestionResponse quiz = response.body().getQuestions().get(0);
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

        QuizResultDialog.newInstance(
                lessonTitle,
                passed,
                scorePercent,
                result.getCorrectCount(),
                result.getTotalQuestions(),
                xpRewarded,
                message,
                buildNextLessonArgs()
        ).show(getParentFragmentManager(), "quiz_result");
    }

    private Bundle buildNextLessonArgs() {
        int nextIndex = lessonIndex + 1;
        if (nextIndex >= lessonList.size()) return null;
        Lesson next = lessonList.get(nextIndex);
        List<Lesson> allLessonList = getAllLessonList(originalArgs);

        Bundle args = new Bundle();
        args.putString("lessonId", next.getLessonID());
        args.putString("lessonTitle", next.getTitle());
        args.putString("courseId", courseId);
        args.putString("contentType", next.getContentType());
        args.putInt("sourceTab", originalArgs != null ? originalArgs.getInt("sourceTab", 0) : 0);
        args.putInt("lessonIndex", nextIndex);
        args.putSerializable("lessonList", new ArrayList<>(lessonList));
        args.putSerializable("allLessonList", new ArrayList<>(allLessonList));

        String nextQuizId = findQuizLessonId(next.getLessonID(), allLessonList);
        if (nextQuizId != null) args.putString("quizLessonId", nextQuizId);

        String content = next.getContent();
        args.putString("lessonContent", (content != null && !content.isEmpty()) ? content : "");
        return args;
    }

    @SuppressWarnings("unchecked")
    private List<Lesson> getAllLessonList(Bundle args) {
        if (args == null) return new ArrayList<>();
        Object raw = args.getSerializable("allLessonList");
        if (raw instanceof ArrayList) return (ArrayList<Lesson>) raw;
        return new ArrayList<>(lessonList);
    }

    private String findQuizLessonId(String parentId, List<Lesson> allLessons) {
        for (Lesson l : allLessons) {
            if (parentId.equals(l.getParentLessonId())) return l.getLessonID();
        }
        return null;
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
        String courseIdArg = originalArgs != null ? originalArgs.getString("courseId", "") : "";
        int sourceTab = originalArgs != null ? originalArgs.getInt("sourceTab", 0) : 0;

        Bundle args = new Bundle();
        args.putString("lessonId", lesson.getLessonID());
        args.putString("lessonTitle", lesson.getTitle());
        args.putString("courseId", courseIdArg);
        args.putString("contentType", lesson.getContentType());
        args.putInt("sourceTab", sourceTab);
        args.putInt("lessonIndex", index);
        args.putSerializable("lessonList", new ArrayList<>(lessonList));

        androidx.navigation.NavController nav = NavHostFragment.findNavController(this);
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
