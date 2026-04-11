package com.eduflex.android.ui.quiz;

import android.content.Context;
import android.content.SharedPreferences;
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

import java.util.Locale;

public class FillBlankQuizMockFragment extends Fragment {

    private static final String PREF_COURSE_PROGRESS = "course_progress";

    private String courseId;
    private String lessonTitle;

    private EditText etAnswer;

    public FillBlankQuizMockFragment() {
        super(R.layout.fragment_fill_blank_quiz_mock);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        courseId = args != null ? args.getString("courseId", "") : "";
        lessonTitle = args != null ? args.getString("lessonTitle", "Quiz điền từ") : "Quiz điền từ";

        TextView tvTitle = view.findViewById(R.id.tv_fill_blank_quiz_title);
        tvTitle.setText(lessonTitle + " - Điền từ (Mock)");

        etAnswer = view.findViewById(R.id.et_fill_blank_answer);

        Button btnBack = view.findViewById(R.id.btn_back_fill_blank_quiz);
        Button btnSubmit = view.findViewById(R.id.btn_submit_fill_blank_quiz);

        btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        btnSubmit.setOnClickListener(v -> submitMockQuiz());
    }

    private void submitMockQuiz() {
        String input = etAnswer.getText() == null ? "" : etAnswer.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(requireContext(), "Nhập câu trả lời trước khi nộp.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mock rule: expected answer is "kotlin".
        String normalized = input.toLowerCase(Locale.US);
        boolean passed = "kotlin".equals(normalized);

        int correctCount = passed ? 1 : 0;
        float scorePercent = passed ? 100f : 0f;
        int xpRewarded = passed ? 30 : 0;

        if (passed) {
            saveMockCourseProgress(20);
        }

        String message = passed
                ? "Mock submit thành công. Bạn đã trả lời đúng câu điền từ."
                : "Mock submit thành công, nhưng câu trả lời chưa đúng.";

        Bundle resultArgs = new Bundle();
        resultArgs.putString("lessonTitle", lessonTitle);
        resultArgs.putString("message", message);
        resultArgs.putInt("correctCount", correctCount);
        resultArgs.putInt("totalQuestions", 1);
        resultArgs.putFloat("scorePercent", scorePercent);
        resultArgs.putInt("xpRewarded", xpRewarded);
        resultArgs.putBoolean("passed", passed);

        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.quizResultFragment, resultArgs);
    }

    private void saveMockCourseProgress(int progressDelta) {
        if (courseId == null || courseId.isEmpty() || !isAdded()) {
            return;
        }

        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_COURSE_PROGRESS, Context.MODE_PRIVATE);
        int current = prefs.getInt(courseId, 0);
        int next = Math.max(0, Math.min(100, current + progressDelta));
        prefs.edit().putInt(courseId, next).apply();
    }
}