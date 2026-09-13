package com.eduflex.android.ui.ai;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.eduflex.android.R;
import com.eduflex.android.api.ApiClient;
import com.eduflex.android.api.CourseApi;
import com.eduflex.android.model.AiCourseSummaryResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AiCourseSummaryFragment extends Fragment {
    private String courseId;
    private String courseTitle;

    private LinearLayout layoutLoading;
    private LinearLayout layoutError;
    private ScrollView scrollSummary;
    private TextView tvSummary;
    private TextView tvError;
    private Call<AiCourseSummaryResponse> pendingCall;
    private CourseApi courseApi;

    public AiCourseSummaryFragment() {
        super(R.layout.fragment_ai_course_summary);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        courseApi = ApiClient.createAuthenticatedService(CourseApi.class);
        if (getArguments() != null) {
            courseId = getArguments().getString("courseId", "");
            courseTitle = getArguments().getString("courseTitle", "");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        layoutLoading = view.findViewById(R.id.layout_loading);
        layoutError = view.findViewById(R.id.layout_error);
        scrollSummary = view.findViewById(R.id.scroll_summary);
        tvSummary = view.findViewById(R.id.tv_summary);
        tvError = view.findViewById(R.id.tv_error);
        ((TextView) view.findViewById(R.id.tv_course_title)).setText(courseTitle);

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());
        view.findViewById(R.id.btn_retry).setOnClickListener(v -> fetchSummary());
        fetchSummary();
    }

    private void fetchSummary() {
        showLoading();
        if (courseId == null || courseId.isBlank()) {
            showError("This course is missing an identifier.");
            return;
        }

        pendingCall = courseApi.getAiSummary(courseId);
        pendingCall.enqueue(new Callback<AiCourseSummaryResponse>() {
            @Override
            public void onResponse(@NonNull Call<AiCourseSummaryResponse> call,
                                   @NonNull Response<AiCourseSummaryResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    showResult(response.body().getSummary());
                } else {
                    showError("The learning summary is unavailable right now.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<AiCourseSummaryResponse> call,
                                  @NonNull Throwable error) {
                if (!isAdded() || call.isCanceled()) return;
                showError("Could not load the summary. Check your connection and try again.");
            }
        });
    }

    private void showLoading() {
        layoutLoading.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.GONE);
        scrollSummary.setVisibility(View.GONE);
    }

    private void showResult(String text) {
        tvSummary.setText(text);
        scrollSummary.setVisibility(View.VISIBLE);
        layoutLoading.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
    }

    private void showError(String message) {
        tvError.setText(message);
        layoutError.setVisibility(View.VISIBLE);
        layoutLoading.setVisibility(View.GONE);
        scrollSummary.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        if (pendingCall != null) pendingCall.cancel();
        super.onDestroyView();
    }
}
