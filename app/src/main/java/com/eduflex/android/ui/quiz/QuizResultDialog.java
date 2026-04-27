package com.eduflex.android.ui.quiz;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.eduflex.android.R;
import com.eduflex.android.api.ApiClient;
import com.eduflex.android.api.GamificationApi;
import com.eduflex.android.auth.TokenManager;
import com.eduflex.android.model.GamificationStatsResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizResultDialog extends DialogFragment {

    private static final String ARG_LESSON_TITLE = "lessonTitle";
    private static final String ARG_PASSED = "passed";
    private static final String ARG_SCORE_PERCENT = "scorePercent";
    private static final String ARG_CORRECT = "correctCount";
    private static final String ARG_TOTAL = "totalQuestions";
    private static final String ARG_XP = "xpRewarded";
    private static final String ARG_MESSAGE = "message";

    public static QuizResultDialog newInstance(String lessonTitle, boolean passed,
                                               float scorePercent, int correctCount,
                                               int totalQuestions, int xpRewarded,
                                               String message) {
        QuizResultDialog dialog = new QuizResultDialog();
        Bundle args = new Bundle();
        args.putString(ARG_LESSON_TITLE, lessonTitle);
        args.putBoolean(ARG_PASSED, passed);
        args.putFloat(ARG_SCORE_PERCENT, scorePercent);
        args.putInt(ARG_CORRECT, correctCount);
        args.putInt(ARG_TOTAL, totalQuestions);
        args.putInt(ARG_XP, xpRewarded);
        args.putString(ARG_MESSAGE, message);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.QuizResultDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_quiz_result, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.88),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        boolean passed = args != null && args.getBoolean(ARG_PASSED, false);
        float scorePercent = args != null ? args.getFloat(ARG_SCORE_PERCENT, 0f) : 0f;
        int correctCount = args != null ? args.getInt(ARG_CORRECT, 0) : 0;
        int totalQuestions = args != null ? args.getInt(ARG_TOTAL, 0) : 0;
        int xpRewarded = args != null ? args.getInt(ARG_XP, 0) : 0;
        String message = args != null ? args.getString(ARG_MESSAGE, "") : "";

        LinearLayout header = view.findViewById(R.id.layout_result_header);
        TextView tvEmoji = view.findViewById(R.id.tv_result_emoji);
        TextView tvStatus = view.findViewById(R.id.tv_result_status);
        TextView tvScore = view.findViewById(R.id.tv_result_score);
        TextView tvXp = view.findViewById(R.id.tv_result_xp);
        TextView tvXpToNext = view.findViewById(R.id.tv_xp_to_next_level);
        TextView tvStar1 = view.findViewById(R.id.tv_star_1);
        TextView tvStar2 = view.findViewById(R.id.tv_star_2);
        TextView tvStar3 = view.findViewById(R.id.tv_star_3);
        Button btnContinue = view.findViewById(R.id.btn_result_continue);
        Button btnRetry = view.findViewById(R.id.btn_result_retry);

        if (passed) {
            header.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.success));
            tvEmoji.setText("🎉");
            tvStatus.setText("Passed!");
        } else {
            header.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.danger));
            tvEmoji.setText("😞");
            tvStatus.setText("Not passed");
            btnRetry.setVisibility(View.VISIBLE);
        }

        int stars = starsForScore(scorePercent);
        int activeColor = ContextCompat.getColor(requireContext(), R.color.fire_active);
        int inactiveColor = ContextCompat.getColor(requireContext(), R.color.fire_inactive);
        tvStar1.setTextColor(stars >= 1 ? activeColor : inactiveColor);
        tvStar2.setTextColor(stars >= 2 ? activeColor : inactiveColor);
        tvStar3.setTextColor(stars >= 3 ? activeColor : inactiveColor);

        String scoreText = String.format("Score: %.0f%%  (%d/%d correct)", scorePercent, correctCount, totalQuestions);
        if (message != null && !message.isEmpty()) {
            scoreText = message + "\n" + scoreText;
        }
        tvScore.setText(scoreText);

        animateXp(tvXp, xpRewarded);
        fetchXpToNextLevel(tvXpToNext, xpRewarded);

        setCancelable(false);
        btnContinue.setOnClickListener(v -> {
            dismiss();
            navigateBackToCourse();
        });
        btnRetry.setOnClickListener(v -> dismiss());
    }

    private void fetchXpToNextLevel(TextView tvXpToNext, int xpJustEarned) {
        if (!isAdded()) return;
        String userId = new TokenManager(requireContext()).getUserId();
        if (userId == null) return;

        GamificationApi api = ApiClient.createAuthenticatedService(GamificationApi.class);
        api.getStats(userId).enqueue(new Callback<GamificationStatsResponse>() {
            @Override
            public void onResponse(@NonNull Call<GamificationStatsResponse> call,
                                   @NonNull Response<GamificationStatsResponse> response) {
                if (!isAdded() || !response.isSuccessful() || response.body() == null) return;
                int currentXp = response.body().getXp();
                int xpInLevel = currentXp % 100;
                int xpToNext = 100 - xpInLevel;
                tvXpToNext.setText(xpToNext + " XP to next level");
                tvXpToNext.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(@NonNull Call<GamificationStatsResponse> call, @NonNull Throwable t) {
                // silently skip — not critical
            }
        });
    }

    private int starsForScore(float score) {
        if (score >= 90) return 3;
        if (score >= 60) return 2;
        if (score > 0) return 1;
        return 0;
    }

    private void animateXp(TextView tvXp, int targetXp) {
        if (targetXp <= 0) {
            tvXp.setText("+0 XP");
            return;
        }
        ValueAnimator animator = ValueAnimator.ofInt(0, targetXp);
        animator.setDuration(800);
        animator.addUpdateListener(a -> tvXp.setText("+" + a.getAnimatedValue() + " XP"));
        animator.start();
    }

    private void navigateBackToCourse() {
        if (!isAdded()) return;
        NavController navController = NavHostFragment.findNavController(this);
        boolean popped = navController.popBackStack(R.id.courseDetailFragment, false);
        if (!popped) {
            navController.navigateUp();
        }
    }
}
