package com.eduflex.android.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eduflex.android.R;
import com.eduflex.android.adapter.AdminCourseAdapter;
import com.eduflex.android.api.AdminApi;
import com.eduflex.android.api.ApiClient;
import com.eduflex.android.api.CourseApi;
import com.eduflex.android.model.Course;
import com.eduflex.android.model.CourseListResponse;
import com.eduflex.android.model.DeleteCourseResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminCoursesFragment extends Fragment {

    private static final String TAG = "AdminCoursesFragment";

    private RecyclerView rvCourses;
    private ProgressBar progressLoading;
    private View llEmptyState;
    private TextView tvTotalCourses;
    private EditText etSearch;

    private AdminApi adminApi;
    private CourseApi courseApi;
    private AdminCourseAdapter adapter;

    public AdminCoursesFragment() {
        super(R.layout.fragment_admin_courses);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adminApi = ApiClient.createAuthenticatedService(AdminApi.class);
        courseApi = ApiClient.createAuthenticatedService(CourseApi.class);
        
        bindViews(view);
        setupSearch();
        fetchCourses();
    }

    private void bindViews(View view) {
        rvCourses = view.findViewById(R.id.rv_admin_courses);
        progressLoading = view.findViewById(R.id.progress_loading_courses);
        llEmptyState = view.findViewById(R.id.ll_empty_courses);
        tvTotalCourses = view.findViewById(R.id.tv_total_courses);
        etSearch = view.findViewById(R.id.et_search_course);

        rvCourses.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchCourses() {
        progressLoading.setVisibility(View.VISIBLE);
        rvCourses.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.GONE);

        courseApi.getCourses().enqueue(new Callback<CourseListResponse>() {
            @Override
            public void onResponse(@NonNull Call<CourseListResponse> call,
                                   @NonNull Response<CourseListResponse> response) {
                if (!isAdded()) return;
                progressLoading.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess()) {
                    List<Course> courses = response.body().getListCourse();
                    if (courses != null && !courses.isEmpty()) {
                        showCourses(courses);
                    } else {
                        showEmptyState();
                    }
                } else {
                    Log.e(TAG, "Failed to load courses: " + response.code());
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CourseListResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                progressLoading.setVisibility(View.GONE);
                Log.e(TAG, "Network error: " + t.getMessage());
                showEmptyState();
            }
        });
    }

    private void showCourses(List<Course> courses) {
        rvCourses.setVisibility(View.VISIBLE);
        llEmptyState.setVisibility(View.GONE);

        tvTotalCourses.setText(String.valueOf(courses.size()));

        adapter = new AdminCourseAdapter(courses, this::confirmDeleteCourse);
        rvCourses.setAdapter(adapter);
    }

    private void confirmDeleteCourse(Course course, int position) {
        if (!isAdded()) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Course")
                .setMessage("Are you sure you want to delete \"" + course.getTitle() + "\"?\n\nThis will also delete ALL lessons, quizzes, and enrollments associated with this course. This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteCourse(course, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteCourse(Course course, int position) {
        adminApi.deleteCourse(course.getCourseID()).enqueue(new Callback<DeleteCourseResponse>() {
            @Override
            public void onResponse(@NonNull Call<DeleteCourseResponse> call,
                                   @NonNull Response<DeleteCourseResponse> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess()) {
                    adapter.removeCourse(position);

                    // Update total count
                    int current = Integer.parseInt(tvTotalCourses.getText().toString());
                    tvTotalCourses.setText(String.valueOf(Math.max(0, current - 1)));

                    Toast.makeText(getContext(), "Course deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Delete failed: " + response.code());
                    Toast.makeText(getContext(), "Failed to delete course", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeleteCourseResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "Delete network error: " + t.getMessage());
                Toast.makeText(getContext(), "Network error while deleting", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEmptyState() {
        rvCourses.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.VISIBLE);
        tvTotalCourses.setText("0");
    }
}
