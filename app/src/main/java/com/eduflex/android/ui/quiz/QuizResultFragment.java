package com.eduflex.android.ui.quiz;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.eduflex.android.R;

import java.util.Locale;

public class QuizResultFragment extends Fragment {

    public QuizResultFragment() {
        super(R.layout.fragment_quiz_result);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        String lessonTitle = args != null ? args.getString("lessonTitle", "Quiz") : "Quiz";
        String message = args != null ? args.getString("message", "Quiz submitted.") : "Quiz submitted.";
        int correctCount = args != null ? args.getInt("correctCount", 0) : 0;
        int totalQuestions = args != null ? args.getInt("totalQuestions", 0) : 0;
        float scorePercent = args != null ? args.getFloat("scorePercent", 0f) : 0f;
        int xpRewarded = args != null ? args.getInt("xpRewarded", 0) : 0;
        boolean passed = args != null && args.getBoolean("passed", false);

        TextView tvTitle = view.findViewById(R.id.tv_quiz_result_title);
        TextView tvStatus = view.findViewById(R.id.tv_quiz_result_status);
        TextView tvSummary = view.findViewById(R.id.tv_quiz_result_summary);
        TextView tvXp = view.findViewById(R.id.tv_quiz_result_xp);

        Button btnBack = view.findViewById(R.id.btn_back_quiz_result);
        Button btnBackToCourse = view.findViewById(R.id.btn_back_to_course_detail);

        tvTitle.setText(lessonTitle + " - Result");
        tvStatus.setText(passed ? "Passed" : "Not passed");
        tvSummary.setText(String.format(
                Locale.US,
                "%s\nScore: %.0f%% (%d/%d correct)",
                message,
                scorePercent,
                correctCount,
                totalQuestions
        ));
        tvXp.setText(String.format(Locale.US, "XP rewarded: %d", xpRewarded));

        btnBack.setOnClickListener(v -> navigateBackToCourseDetail());
        btnBackToCourse.setOnClickListener(v -> navigateBackToCourseDetail());
    }

    private void navigateBackToCourseDetail() {
        NavController navController = NavHostFragment.findNavController(this);
        boolean popped = navController.popBackStack(R.id.courseDetailFragment, false);
        if (!popped) {
            navController.navigateUp();
        }
    }
}
