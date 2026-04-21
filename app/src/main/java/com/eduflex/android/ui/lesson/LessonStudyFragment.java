package com.eduflex.android.ui.lesson;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.eduflex.android.R;
import com.eduflex.android.api.ApiClient;
import com.eduflex.android.api.ProgressApi;
import com.eduflex.android.auth.TokenManager;
import com.eduflex.android.model.SaveProgressResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LessonStudyFragment extends Fragment {

    private static final String TAG = "LessonStudyFragment";
    private static final String PREF_COURSE_PROGRESS = "course_progress";

    public LessonStudyFragment() {
        super(R.layout.fragment_lesson_study);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        String lessonId = args != null ? args.getString("lessonId", "") : "";
        String lessonTitle = args != null ? args.getString("lessonTitle", "Untitled Lesson") : "Untitled Lesson";
        String contentType = args != null ? args.getString("contentType", "reading") : "reading";
        String lessonContent = args != null ? args.getString("lessonContent", "") : "";
        String courseId = args != null ? args.getString("courseId", "") : "";

        TextView tvTitle = view.findViewById(R.id.tv_lesson_study_title);
        TextView tvType = view.findViewById(R.id.tv_lesson_study_type);
        TextView tvTextContent = view.findViewById(R.id.tv_lesson_text_content);
        ImageView ivVideoPlaceholder = view.findViewById(R.id.iv_video_placeholder);
        Button btnComplete = view.findViewById(R.id.btn_mark_complete);

        view.findViewById(R.id.btn_back_lesson).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());

        tvTitle.setText(lessonTitle);
        tvType.setText(contentType.toUpperCase());

        String normalizedType = contentType.toLowerCase();
        if ("video".equals(normalizedType)) {
            ivVideoPlaceholder.setVisibility(View.VISIBLE);
            tvTextContent.setVisibility(View.GONE);
        } else {
            ivVideoPlaceholder.setVisibility(View.GONE);
            tvTextContent.setVisibility(View.VISIBLE);
            tvTextContent.setText(lessonContent.isEmpty() ? "No text content available." : lessonContent);
        }

        btnComplete.setOnClickListener(v -> markLessonComplete(lessonId, courseId, btnComplete));
    }

    private void markLessonComplete(String lessonId, String courseId, Button btnComplete) {
        TokenManager tokenManager = new TokenManager(requireContext());
        String userId = tokenManager.getUserId();

        if (lessonId == null || lessonId.isEmpty() || userId == null) {
            Toast.makeText(requireContext(), "Cannot sync progress: missing lesson or user info.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnComplete.setEnabled(false);
        btnComplete.setText("Saving...");

        ProgressApi progressApi = ApiClient.createAuthenticatedService(ProgressApi.class);

        progressApi.saveLessonProgress(lessonId, userId).enqueue(new Callback<SaveProgressResponse>() {
            @Override
            public void onResponse(@NonNull Call<SaveProgressResponse> call,
                                   @NonNull Response<SaveProgressResponse> response) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Double percent = response.body().getNewProgressPercent();
                        if (percent != null && !courseId.isEmpty()) {
                            SharedPreferences prefs = requireContext()
                                    .getSharedPreferences(PREF_COURSE_PROGRESS, Context.MODE_PRIVATE);
                            prefs.edit().putInt(courseId, percent.intValue()).apply();
                        }
                        btnComplete.setText("Completed ✓");
                        Toast.makeText(requireContext(), "Progress saved!", Toast.LENGTH_SHORT).show();
                    } else {
                        btnComplete.setEnabled(true);
                        btnComplete.setText("Mark as Complete");
                        Toast.makeText(requireContext(), "Failed to save progress.", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Progress save failed: " + (response.body() != null ? response.body().getMessage() : response.code()));
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<SaveProgressResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    btnComplete.setEnabled(true);
                    btnComplete.setText("Mark as Complete");
                    Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Progress sync failed: " + t.getMessage());
                });
            }
        });
    }
}
