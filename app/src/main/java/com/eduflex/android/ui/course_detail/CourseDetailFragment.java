package com.eduflex.android.ui.course_detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.eduflex.android.R;
import com.eduflex.android.adapter.LessonAdapter;
import com.eduflex.android.api.ApiClient;
import com.eduflex.android.api.LessonApi;
import com.eduflex.android.model.Lesson;
import com.eduflex.android.model.LessonListResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class CourseDetailFragment extends Fragment {

    private String courseId;
    private String courseTitle;
    private String courseDescription;
    private LessonApi lessonApi;
    private RecyclerView rvLessons;
    private TextView tvLessonsEmpty;

    public CourseDetailFragment() {
        super(R.layout.fragment_course_detail);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lessonApi = ApiClient.createAuthenticatedService(LessonApi.class);
        if (getArguments() != null) {
            courseId = getArguments().getString("courseId", "");
            courseTitle = getArguments().getString("courseTitle", "Course Title");
            courseDescription = getArguments().getString("courseDescription", "Course Description");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup back button
        Button btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.popBackStack();
        });

        // Setup header
        TextView tvTitle = view.findViewById(R.id.tv_course_title);
        TextView tvDescription = view.findViewById(R.id.tv_course_description);
        
        tvTitle.setText(courseTitle);
        tvDescription.setText(courseDescription);

        rvLessons = view.findViewById(R.id.rv_lessons);
        tvLessonsEmpty = view.findViewById(R.id.tv_lessons_empty);
        rvLessons.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Load lessons from API
        loadLessons();
    }

    private void loadLessons() {
        if (courseId == null || courseId.isEmpty()) {
            showEmptyState("Lessons are being updated. Please check back later.");
            return;
        }

        // Call API to get lessons for this course
        lessonApi.getLessons(courseId).enqueue(new Callback<LessonListResponse>() {
            @Override
            public void onResponse(Call<LessonListResponse> call, Response<LessonListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LessonListResponse lessonResponse = response.body();
                    if (lessonResponse.isSuccess()) {
                        List<Lesson> lessons = lessonResponse.getListLesson();
                        if (lessons == null || lessons.isEmpty()) {
                            showEmptyState("Lessons are being updated. Please check back later.");
                        } else {
                            rvLessons.setVisibility(View.VISIBLE);
                            tvLessonsEmpty.setVisibility(View.GONE);
                            rvLessons.setAdapter(new LessonAdapter(lessons, CourseDetailFragment.this::openLesson));
                        }
                    } else {
                        showError("Failed to load lessons: " + lessonResponse.getMessage());
                    }
                } else {
                    showError("Error loading lessons from server");
                }
            }

            @Override
            public void onFailure(Call<LessonListResponse> call, Throwable t) {
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showEmptyState(String message) {
        rvLessons.setVisibility(View.GONE);
        tvLessonsEmpty.setText(message);
        tvLessonsEmpty.setVisibility(View.VISIBLE);
    }

    private void openLesson(Lesson lesson) {
        Bundle args = new Bundle();
        args.putString("lessonId", lesson.getLessonID());
        args.putString("lessonTitle", lesson.getTitle());
        args.putString("contentType", lesson.getContentType());
        NavController navController = NavHostFragment.findNavController(this);

        String type = lesson.getContentType() == null ? "" : lesson.getContentType().toLowerCase();
        if ("quiz".equals(type)) {
            navController.navigate(R.id.quizFragment, args);
            return;
        }

        args.putString("lessonContent", getMockContent(lesson.getTitle(), type));
        navController.navigate(R.id.lessonStudyFragment, args);
    }

    private String getMockContent(String lessonTitle, String contentType) {
        if ("video".equals(contentType)) {
            return "VIDEO_PLACEHOLDER";
        }
        return "This is lesson content for: " + lessonTitle
                + "\n\nIn this lesson, you will learn key concepts and practical examples."
                + "\n\n- Topic overview"
                + "\n- Main ideas"
                + "\n- Practical notes";
    }
}
