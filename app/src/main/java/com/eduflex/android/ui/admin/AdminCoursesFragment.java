package com.eduflex.android.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
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
import com.eduflex.android.model.UpdateCourseRequest;
import com.eduflex.android.model.UpdateCourseResponse;

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

        adapter = new AdminCourseAdapter(courses,
                this::confirmDeleteCourse,
                this::showEditCourseDialog,
                this::navigateToLessons);
        rvCourses.setAdapter(adapter);
    }

    // ===== Navigate to Lessons =====
    private void navigateToLessons(Course course) {
        Bundle args = new Bundle();
        args.putString("courseId", course.getCourseID());
        args.putString("courseTitle", course.getTitle());

        Navigation.findNavController(requireView())
                .navigate(R.id.action_courses_to_lessons, args);
    }

    // ===== Edit Course Dialog =====
    private void showEditCourseDialog(Course course, int position) {
        if (!isAdded()) return;

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        addLabel(layout, "Title");
        EditText etTitle = new EditText(requireContext());
        etTitle.setHint("Enter course title");
        etTitle.setText(course.getTitle());
        layout.addView(etTitle);

        addLabel(layout, "Learning Model");
        EditText etModel = new EditText(requireContext());
        etModel.setHint("e.g. Visual, Reading, Kinesthetic");
        etModel.setText(course.getLearningMode());
        layout.addView(etModel);

        addLabel(layout, "Status");
        EditText etStatus = new EditText(requireContext());
        etStatus.setHint("draft / active");
        etStatus.setText(course.getStatus());
        layout.addView(etStatus);

        addLabel(layout, "Description");
        EditText etDescription = new EditText(requireContext());
        etDescription.setHint("Enter course description");
        etDescription.setText(course.getDescription());
        etDescription.setMinLines(2);
        layout.addView(etDescription);

        addLabel(layout, "Image URL");
        EditText etImageUrl = new EditText(requireContext());
        etImageUrl.setHint("https://...");
        etImageUrl.setText(course.getImageUrl());
        layout.addView(etImageUrl);

        addLabel(layout, "Price");
        EditText etPrice = new EditText(requireContext());
        etPrice.setHint("0");
        etPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        if (course.getPrice() != null) {
            etPrice.setText(String.valueOf(course.getPrice()));
        }
        layout.addView(etPrice);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Course")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String model = etModel.getText().toString().trim();
                    String status = etStatus.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();
                    String imageUrl = etImageUrl.getText().toString().trim();
                    Long price = null;
                    try {
                        if (!etPrice.getText().toString().trim().isEmpty()) {
                            price = Long.parseLong(etPrice.getText().toString().trim());
                        }
                    } catch (NumberFormatException ignored) {}

                    if (title.isEmpty()) {
                        Toast.makeText(getContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    UpdateCourseRequest request = new UpdateCourseRequest(
                            title,
                            model.isEmpty() ? null : model,
                            status.isEmpty() ? null : status,
                            imageUrl.isEmpty() ? null : imageUrl,
                            price,
                            description.isEmpty() ? null : description
                    );

                    adminApi.updateCourse(course.getCourseID(), request)
                            .enqueue(new Callback<UpdateCourseResponse>() {
                                @Override
                                public void onResponse(@NonNull Call<UpdateCourseResponse> call,
                                                       @NonNull Response<UpdateCourseResponse> response) {
                                    if (!isAdded()) return;
                                    if (response.isSuccessful() && response.body() != null
                                            && response.body().isSuccess()) {
                                        // Update local data
                                        Course updated = new Course(
                                                course.getCourseID(), title,
                                                model.isEmpty() ? course.getLearningMode() : model,
                                                status.isEmpty() ? course.getStatus() : status
                                        );
                                        adapter.updateCourse(position, updated);
                                        Toast.makeText(getContext(), "Course updated", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(), "Failed to update", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<UpdateCourseResponse> call, @NonNull Throwable t) {
                                    if (!isAdded()) return;
                                    Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ===== Delete Course =====
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

                    int current = Integer.parseInt(tvTotalCourses.getText().toString());
                    tvTotalCourses.setText(String.valueOf(Math.max(0, current - 1)));

                    Toast.makeText(getContext(), "Course deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to delete course", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeleteCourseResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Network error while deleting", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEmptyState() {
        rvCourses.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.VISIBLE);
        tvTotalCourses.setText("0");
    }

    private void addLabel(LinearLayout parent, String text) {
        TextView label = new TextView(requireContext());
        label.setText(text);
        label.setTextSize(13);
        label.setTextColor(getResources().getColor(R.color.text_title, null));
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = 16;
        label.setLayoutParams(params);
        parent.addView(label);
    }
}
