package com.eduflex.android.ui.quiz;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizFragment extends Fragment {

    private static final String PREF_COURSE_PROGRESS = "course_progress";

    private QuizApi quizApi;
    private TokenManager tokenManager;
    private String lessonId;
    private String courseId;
    private int lessonIndex;
    private List<Lesson> lessonList = new ArrayList<>();
    private Bundle originalArgs;

    private TextView tvQuizTitle;
    private LinearLayout llQuestionsContainer;
    private Button btnSubmit;

    // questionId -> selected optionId
    private final Map<Long, Long> selectedAnswers = new HashMap<>();
    // questionId -> list of options (for answer lookup)
    private List<QuizGetResponse.QuestionResponse> loadedQuestions = new ArrayList<>();

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
        llQuestionsContainer = view.findViewById(R.id.ll_questions_container);
        btnSubmit = view.findViewById(R.id.btn_submit_quiz);
        Button btnPrev = view.findViewById(R.id.btn_prev_lesson);
        Button btnNext = view.findViewById(R.id.btn_next_lesson);

        tvQuizTitle.setText(lessonTitle + " - Quiz");

        view.findViewById(R.id.btn_back_quiz).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());

        btnPrev.setEnabled(lessonIndex > 0);
        btnNext.setEnabled(lessonIndex < lessonList.size() - 1);
        btnPrev.setOnClickListener(v -> navigateToLesson(lessonIndex - 1));
        btnNext.setOnClickListener(v -> navigateToLesson(lessonIndex + 1));

        if (lessonId == null || lessonId.isEmpty()) {
            showError("Quiz is unavailable for this lesson.");
            return;
        }

        loadQuiz();
        btnSubmit.setOnClickListener(v -> submitQuiz());
    }

    private void loadQuiz() {
        btnSubmit.setEnabled(false);
        llQuestionsContainer.removeAllViews();

        quizApi.getQuiz(lessonId).enqueue(new Callback<QuizGetResponse>() {
            @Override
            public void onResponse(@NonNull Call<QuizGetResponse> call, @NonNull Response<QuizGetResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    loadedQuestions = response.body().getQuestions();
                    if (loadedQuestions == null || loadedQuestions.isEmpty()) {
                        showError("No quiz questions found for this lesson.");
                    } else {
                        buildQuestionCards(loadedQuestions);
                        btnSubmit.setEnabled(true);
                    }
                } else {
                    String msg = "Failed to load quiz.";
                    if (response.body() != null && response.body().getMessage() != null
                            && !response.body().getMessage().trim().isEmpty()) {
                        msg = response.body().getMessage();
                    }
                    showError(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<QuizGetResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                showError("Network error while loading quiz.");
            }
        });
    }

    private void buildQuestionCards(List<QuizGetResponse.QuestionResponse> questions) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        llQuestionsContainer.removeAllViews();

        for (int qi = 0; qi < questions.size(); qi++) {
            QuizGetResponse.QuestionResponse q = questions.get(qi);

            // Card wrapper
            CardView card = new CardView(requireContext());
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.bottomMargin = dpToPx(16);
            card.setLayoutParams(cardParams);
            card.setRadius(dpToPx(14));
            card.setCardElevation(0);
            card.setCardBackgroundColor(getResources().getColor(R.color.card_background, null));

            LinearLayout cardContent = new LinearLayout(requireContext());
            cardContent.setOrientation(LinearLayout.VERTICAL);
            int pad = dpToPx(16);
            cardContent.setPadding(pad, pad, pad, pad);

            // Question number + text
            TextView tvQuestion = new TextView(requireContext());
            tvQuestion.setText((qi + 1) + ". " + q.getQuestionText());
            tvQuestion.setTextSize(16);
            tvQuestion.setTextColor(getResources().getColor(R.color.text_primary, null));
            tvQuestion.setTypeface(null, android.graphics.Typeface.BOLD);
            cardContent.addView(tvQuestion);

            // Radio group for options
            RadioGroup rg = new RadioGroup(requireContext());
            rg.setOrientation(RadioGroup.VERTICAL);
            LinearLayout.LayoutParams rgParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rgParams.topMargin = dpToPx(12);
            rg.setLayoutParams(rgParams);

            List<QuizGetResponse.OptionResponse> options = q.getOptions();
            if (options != null) {
                for (QuizGetResponse.OptionResponse opt : options) {
                    RadioButton rb = new RadioButton(requireContext());
                    rb.setText(opt.getOptionText());
                    rb.setTextSize(15);
                    rb.setTextColor(getResources().getColor(R.color.text_title, null));
                    rb.setButtonTintList(android.content.res.ColorStateList.valueOf(
                            getResources().getColor(R.color.cyan_primary_dark, null)));
                    rb.setBackgroundColor(getResources().getColor(R.color.cyan_surface, null));
                    int rbPad = dpToPx(12);
                    rb.setPadding(rbPad, rbPad, rbPad, rbPad);
                    LinearLayout.LayoutParams rbParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    rbParams.bottomMargin = dpToPx(8);
                    rb.setLayoutParams(rbParams);
                    rg.addView(rb);
                }
            }

            long questionId = q.getQuestionId();
            rg.setOnCheckedChangeListener((group, checkedId) -> {
                int index = group.indexOfChild(group.findViewById(checkedId));
                if (options != null && index >= 0 && index < options.size()) {
                    selectedAnswers.put(questionId, options.get(index).getOptionId());
                }
            });

            cardContent.addView(rg);
            card.addView(cardContent);
            llQuestionsContainer.addView(card);
        }
    }

    private void submitQuiz() {
        if (loadedQuestions == null || loadedQuestions.isEmpty()) return;

        // Ensure all questions answered
        for (QuizGetResponse.QuestionResponse q : loadedQuestions) {
            if (!selectedAnswers.containsKey(q.getQuestionId())) {
                Toast.makeText(requireContext(),
                        "Please answer all questions before submitting.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String userId = tokenManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(requireContext(), "Please login again to submit quiz.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<SubmitQuizRequest.AnswerItem> answers = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : selectedAnswers.entrySet()) {
            answers.add(new SubmitQuizRequest.AnswerItem(entry.getKey(), entry.getValue()));
        }

        SubmitQuizRequest request = new SubmitQuizRequest(userId, lessonId, answers);
        btnSubmit.setEnabled(false);

        quizApi.submitQuiz(request).enqueue(new Callback<SubmitQuizResponse>() {
            @Override
            public void onResponse(@NonNull Call<SubmitQuizResponse> call, @NonNull Response<SubmitQuizResponse> response) {
                if (!isAdded()) return;
                btnSubmit.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    saveCourseProgress(response.body().getCourseProgress());
                    navigateToResultScreen(response.body());
                } else {
                    Toast.makeText(requireContext(), "Failed to submit quiz.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SubmitQuizResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                btnSubmit.setEnabled(true);
                Toast.makeText(requireContext(), "Submit failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showError(String message) {
        llQuestionsContainer.removeAllViews();
        TextView tv = new TextView(requireContext());
        tv.setText(message);
        tv.setTextSize(15);
        tv.setTextColor(getResources().getColor(R.color.text_primary, null));
        tv.setPadding(0, dpToPx(8), 0, dpToPx(8));
        llQuestionsContainer.addView(tv);
        btnSubmit.setEnabled(false);
    }

    private void saveCourseProgress(Double courseProgress) {
        if (courseProgress == null || courseId == null || courseId.isEmpty() || !isAdded()) return;
        int progress = Math.max(0, Math.min(100, (int) Math.round(courseProgress)));
        requireContext().getSharedPreferences(PREF_COURSE_PROGRESS, Context.MODE_PRIVATE)
                .edit().putInt(courseId, progress).apply();
    }

    private void navigateToResultScreen(SubmitQuizResponse result) {
        if (!isAdded()) return;
        String lessonTitle = tvQuizTitle.getText().toString().replace(" - Quiz", "");
        String message = result.getMessage() == null ? "Quiz submitted." : result.getMessage();
        Bundle nextArgs = buildNextLessonArgs();
        QuizResultDialog.newInstance(
                lessonTitle,
                result.isPassed(),
                (float) result.getScorePercent(),
                result.getCorrectCount(),
                result.getTotalQuestions(),
                result.getXpRewarded(),
                message,
                nextArgs
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
        List<Lesson> allLessonList = getAllLessonList(originalArgs);

        Bundle args = new Bundle();
        args.putString("lessonId", lesson.getLessonID());
        args.putString("lessonTitle", lesson.getTitle());
        args.putString("courseId", courseId);
        args.putString("contentType", lesson.getContentType());
        args.putInt("sourceTab", originalArgs != null ? originalArgs.getInt("sourceTab", 0) : 0);
        args.putInt("lessonIndex", index);
        args.putSerializable("lessonList", new ArrayList<>(lessonList));
        args.putSerializable("allLessonList", new ArrayList<>(allLessonList));

        String quizId = findQuizLessonId(lesson.getLessonID(), allLessonList);
        if (quizId != null) args.putString("quizLessonId", quizId);

        String content = lesson.getContent();
        args.putString("lessonContent", (content != null && !content.isEmpty()) ? content : "");

        NavHostFragment.findNavController(this).navigate(R.id.lessonStudyFragment, args);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
