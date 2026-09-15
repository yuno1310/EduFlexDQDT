package com.eduflex.android.ui.courses;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eduflex.android.R;
import com.eduflex.android.adapter.EnrolledCourseAdapter;
import com.eduflex.android.api.ApiClient;
import com.eduflex.android.api.CourseApi;
import com.eduflex.android.auth.TokenManager;
import com.eduflex.android.model.EnrolledCourse;
import com.eduflex.android.model.EnrolledCoursesResponse;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CoursesFragment extends Fragment {

    private static final String TAG = "CoursesFragment";

    private RecyclerView rvEnrolledCourses;
    private TextView tvCoursesEmpty;
    private ProgressBar progressBar;
    private MaterialButton actionButton;
    private Call<EnrolledCoursesResponse> pendingCall;

    private CourseApi courseApi;
    private TokenManager tokenManager;

    public CoursesFragment() {
        super(R.layout.fragment_courses);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        courseApi = ApiClient.createAuthenticatedService(CourseApi.class);
        tokenManager = new TokenManager(requireContext());

        rvEnrolledCourses = view.findViewById(R.id.rv_enrolled_courses);
        tvCoursesEmpty = view.findViewById(R.id.tv_courses_empty);
        progressBar = view.findViewById(R.id.progress_bar_courses);
        actionButton = view.findViewById(R.id.btn_courses_action);

        rvEnrolledCourses.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadEnrolledCourses();
    }

    private void loadEnrolledCourses() {
        String userId = tokenManager.getUserId();
        if (userId == null) {
            showEmpty("Please log in to see your courses.");
            return;
        }

        setLoading(true);
        if (pendingCall != null) pendingCall.cancel();
        pendingCall = courseApi.getEnrolledCourses(userId);
        pendingCall.enqueue(new Callback<EnrolledCoursesResponse>() {
            @Override
            public void onResponse(@NonNull Call<EnrolledCoursesResponse> call,
                                   @NonNull Response<EnrolledCoursesResponse> response) {
                if (rvEnrolledCourses == null || call != pendingCall || call.isCanceled()) return;
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<EnrolledCourse> courses = response.body().getEnrolledCourses();
                    if (courses == null || courses.isEmpty()) {
                        showEmpty("You haven't enrolled in any courses yet.");
                        actionButton.setText(R.string.browse_courses);
                        actionButton.setVisibility(View.VISIBLE);
                        actionButton.setOnClickListener(v -> NavHostFragment.findNavController(CoursesFragment.this)
                                .navigate(R.id.searchFragment));
                    } else {
                        rvEnrolledCourses.setAdapter(new EnrolledCourseAdapter(courses, CoursesFragment.this::openCourseDetail));
                        rvEnrolledCourses.setVisibility(View.VISIBLE);
                        tvCoursesEmpty.setVisibility(View.GONE);
                    }
                } else {
                    showError();
                }
            }

            @Override
            public void onFailure(@NonNull Call<EnrolledCoursesResponse> call,
                                  @NonNull Throwable t) {
                if (rvEnrolledCourses == null || call != pendingCall || call.isCanceled()) return;
                setLoading(false);
                Log.e(TAG, "Network error: " + t.getMessage());
                showError();
            }
        });
    }

    private void openCourseDetail(EnrolledCourse course) {
        Bundle args = new Bundle();
        args.putString("courseId", course.getCourseId());
        args.putString("courseTitle", course.getTitle() != null ? course.getTitle() : "");
        args.putString("courseDescription", course.getLearningMode() != null ? course.getLearningMode() : "");
        args.putInt("progressPercent", (int) Math.round(course.getProgressPercent()));
        args.putInt("sourceTab", R.id.coursesFragment);
        NavHostFragment.findNavController(this).navigate(R.id.courseDetailFragment, args);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) {
            actionButton.setVisibility(View.GONE);
            rvEnrolledCourses.setVisibility(View.GONE);
            tvCoursesEmpty.setVisibility(View.GONE);
        }
    }

    private void showEmpty(String message) {
        rvEnrolledCourses.setVisibility(View.GONE);
        tvCoursesEmpty.setText(message);
        tvCoursesEmpty.setVisibility(View.VISIBLE);
    }

    private void showError() {
        showEmpty(getString(R.string.search_load_error));
        actionButton.setText(R.string.search_retry);
        actionButton.setVisibility(View.VISIBLE);
        actionButton.setOnClickListener(v -> loadEnrolledCourses());
    }

    @Override
    public void onDestroyView() {
        if (pendingCall != null) pendingCall.cancel();
        pendingCall = null;
        rvEnrolledCourses.setAdapter(null);
        rvEnrolledCourses = null;
        tvCoursesEmpty = null;
        progressBar = null;
        actionButton = null;
        super.onDestroyView();
    }
}
