package com.eduflex.android.ui.course_detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.eduflex.android.api.CourseApi;
import com.eduflex.android.api.LessonApi;
import com.eduflex.android.auth.TokenManager;
import com.eduflex.android.cart.CartManager;
import com.eduflex.android.model.CartItem;
import com.eduflex.android.model.EnrolledCoursesResponse;
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
    private int initialProgress;
    private int sourceTab;
    private LessonApi lessonApi;
    private CourseApi courseApi;
    private RecyclerView rvLessons;
    private TextView tvLessonsEmpty;
    private ProgressBar progressCourse;
    private TextView tvCourseProgressValue;
    private Button btnEnroll;
    private LinearLayout llEnrolledContent;

    public CourseDetailFragment() {
        super(R.layout.fragment_course_detail);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lessonApi = ApiClient.createAuthenticatedService(LessonApi.class);
        courseApi = ApiClient.createAuthenticatedService(CourseApi.class);
        if (getArguments() != null) {
            courseId = getArguments().getString("courseId", "");
            courseTitle = getArguments().getString("courseTitle", "Course Title");
            courseDescription = getArguments().getString("courseDescription", "Course Description");
            initialProgress = getArguments().getInt("progressPercent", -1);
            sourceTab = getArguments().getInt("sourceTab", 0);
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

        Button btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        TextView tvTitle = view.findViewById(R.id.tv_course_title);
        TextView tvDescription = view.findViewById(R.id.tv_course_description);
        tvTitle.setText(courseTitle);
        tvDescription.setText(courseDescription);

        btnEnroll = view.findViewById(R.id.btn_enroll);
        llEnrolledContent = view.findViewById(R.id.ll_enrolled_content);

        Button btnAiSummary = view.findViewById(R.id.btn_ai_summary);
        btnAiSummary.setOnClickListener(v -> openAiSummary());

        Button btnCourseReview = view.findViewById(R.id.btn_course_review);
        btnCourseReview.setOnClickListener(v -> openCourseReview());
        Button btnCourseCertificate = view.findViewById(R.id.btn_course_certificate);
        btnCourseCertificate.setOnClickListener(v -> openCertificate());

        rvLessons = view.findViewById(R.id.rv_lessons);
        tvLessonsEmpty = view.findViewById(R.id.tv_lessons_empty);
        progressCourse = view.findViewById(R.id.progress_course);
        tvCourseProgressValue = view.findViewById(R.id.tv_course_progress_value);
        rvLessons.setLayoutManager(new LinearLayoutManager(getContext()));

        checkEnrollmentAndSetup();
    }

    private void checkEnrollmentAndSetup() {
        String userId = new TokenManager(requireContext()).getUserId();
        if (userId == null) {
            showUnenrolledState();
            return;
        }

        courseApi.getEnrolledCourses(userId).enqueue(new Callback<EnrolledCoursesResponse>() {
            @Override
            public void onResponse(@NonNull Call<EnrolledCoursesResponse> call,
                                   @NonNull Response<EnrolledCoursesResponse> response) {
                if (!isAdded()) return;
                boolean enrolled = false;
                if (response.isSuccessful() && response.body() != null
                        && response.body().getEnrolledCourses() != null) {
                    for (var c : response.body().getEnrolledCourses()) {
                        if (courseId.equals(c.getCourseId())) {
                            enrolled = true;
                            break;
                        }
                    }
                }
                if (enrolled) {
                    showEnrolledState();
                } else {
                    showUnenrolledState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<EnrolledCoursesResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                showUnenrolledState();
            }
        });
    }

    private void showEnrolledState() {
        btnEnroll.setVisibility(View.GONE);
        llEnrolledContent.setVisibility(View.VISIBLE);
        updateCourseProgressUi();
        loadLessons();
    }

    private void showUnenrolledState() {
        llEnrolledContent.setVisibility(View.GONE);
        updateAddToCartButton(btnEnroll);
        btnEnroll.setOnClickListener(v -> addToCart(btnEnroll));
        btnEnroll.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (llEnrolledContent != null && llEnrolledContent.getVisibility() == View.VISIBLE) {
            updateCourseProgressUi();
        }
    }

    private void updateAddToCartButton(Button btn) {
        if (CartManager.getInstance().contains(courseId)) {
            btn.setText("In Cart ✓");
            btn.setEnabled(false);
        } else {
            btn.setText("Add to Cart");
            btn.setEnabled(true);
        }
    }

    private void addToCart(Button btn) {
        CartManager.getInstance().addItem(new CartItem(courseId, courseTitle, ""));
        btn.setText("In Cart ✓");
        btn.setEnabled(false);
        Toast.makeText(requireContext(), courseTitle + " added to cart.", Toast.LENGTH_SHORT).show();
    }

    private void loadLessons() {
        if (courseId == null || courseId.isEmpty()) {
            showEmptyState("Lessons are being updated. Please check back later.");
            return;
        }

        lessonApi.getLessons(courseId).enqueue(new Callback<LessonListResponse>() {
            @Override
            public void onResponse(Call<LessonListResponse> call, Response<LessonListResponse> response) {
                if (!isAdded()) return;
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
                if (!isAdded()) return;
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
        args.putString("courseId", courseId);
        args.putString("contentType", lesson.getContentType());
        args.putInt("sourceTab", sourceTab);
        NavController navController = NavHostFragment.findNavController(this);

        String type = lesson.getContentType() == null ? "" : lesson.getContentType().toLowerCase();
        if ("quiz_fill_blank".equals(type) || "quiz_dien_tu".equals(type)
                || "quiz_new".equals(type) || "quiz_new_type".equals(type)) {
            navController.navigate(R.id.fillBlankQuizMockFragment, args);
            return;
        }
        if ("quiz".equals(type)) {
            navController.navigate(R.id.quizFragment, args);
            return;
        }
        args.putString("lessonContent", getMockContent(lesson.getTitle(), type));
        navController.navigate(R.id.lessonStudyFragment, args);
    }

    private void openAiSummary() {
        Bundle args = new Bundle();
        args.putString("courseId", courseId == null ? "" : courseId);
        args.putString("courseTitle", courseTitle == null ? "" : courseTitle);
        args.putString("courseDescription", courseDescription == null ? "" : courseDescription);
        args.putInt("sourceTab", sourceTab);
        NavHostFragment.findNavController(this).navigate(R.id.aiCourseSummaryFragment, args);
    }

    private void openCourseReview() {
        Bundle args = new Bundle();
        args.putString("courseId", courseId == null ? "" : courseId);
        args.putString("courseTitle", courseTitle == null ? "Course" : courseTitle);
        args.putInt("sourceTab", sourceTab);
        NavHostFragment.findNavController(this).navigate(R.id.courseReviewFragment, args);
    }

    private void openCertificate() {
        int progress = getCurrentCourseProgress();
        if (progress < 100) {
            Toast.makeText(requireContext(), "Complete 100% progress to unlock certificate.", Toast.LENGTH_SHORT).show();
            return;
        }
        Bundle args = new Bundle();
        args.putString("courseId", courseId == null ? "" : courseId);
        args.putString("courseTitle", courseTitle == null ? "Course" : courseTitle);
        args.putInt("sourceTab", sourceTab);
        NavHostFragment.findNavController(this).navigate(R.id.certificateFragment, args);
    }

    private void updateCourseProgressUi() {
        if (progressCourse == null || tvCourseProgressValue == null) return;
        int progress = getCurrentCourseProgress();
        progressCourse.setProgress(progress);
        tvCourseProgressValue.setText(progress + "%");
    }

    private int getCurrentCourseProgress() {
        return Math.max(0, initialProgress);
    }

    private String getMockContent(String lessonTitle, String contentType) {
        if ("video".equals(contentType)) return "VIDEO_PLACEHOLDER";
        return "This is lesson content for: " + lessonTitle
                + "\n\nIn this lesson, you will learn key concepts and practical examples."
                + "\n\n- Topic overview"
                + "\n- Main ideas"
                + "\n- Practical notes";
    }
}
