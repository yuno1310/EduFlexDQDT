package com.eduflex.android.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.eduflex.android.adapter.AdminLessonAdapter;
import com.eduflex.android.api.AdminApi;
import com.eduflex.android.api.ApiClient;
import com.eduflex.android.api.LessonApi;
import com.eduflex.android.api.QuizApi;
import com.eduflex.android.model.DeleteCourseResponse;
import com.eduflex.android.model.Lesson;
import com.eduflex.android.model.LessonListResponse;
import com.eduflex.android.model.QuizGetResponse;
import com.eduflex.android.model.UpdateLessonRequest;
import com.eduflex.android.model.UpdateLessonResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminLessonsFragment extends Fragment {

    private static final String TAG = "AdminLessonsFragment";

    private RecyclerView rvLessons;
    private ProgressBar progressLoading;
    private View llEmptyState;
    private TextView tvTotalLessons;
    private TextView tvCourseName;

    private AdminApi adminApi;
    private LessonApi lessonApi;
    private QuizApi quizApi;
    private AdminLessonAdapter adapter;

    private String courseId;
    private String courseTitle;

    public AdminLessonsFragment() {
        super(R.layout.fragment_admin_lessons);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get args
        if (getArguments() != null) {
            courseId = getArguments().getString("courseId");
            courseTitle = getArguments().getString("courseTitle", "Course");
        }

        adminApi = ApiClient.createAuthenticatedService(AdminApi.class);
        lessonApi = ApiClient.createAuthenticatedService(LessonApi.class);
        quizApi = ApiClient.createAuthenticatedService(QuizApi.class);

        bindViews(view);
        fetchLessons();
    }

    private void bindViews(View view) {
        rvLessons = view.findViewById(R.id.rv_admin_lessons);
        progressLoading = view.findViewById(R.id.progress_loading_lessons);
        llEmptyState = view.findViewById(R.id.ll_empty_lessons);
        tvTotalLessons = view.findViewById(R.id.tv_total_lessons);
        tvCourseName = view.findViewById(R.id.tv_course_name);
        ImageButton btnBack = view.findViewById(R.id.btn_back);

        tvCourseName.setText(courseTitle);
        rvLessons.setLayoutManager(new LinearLayoutManager(getContext()));

        btnBack.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigateUp();
        });
    }

    private void fetchLessons() {
        progressLoading.setVisibility(View.VISIBLE);
        rvLessons.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.GONE);

        lessonApi.getLessons(courseId).enqueue(new Callback<LessonListResponse>() {
            @Override
            public void onResponse(@NonNull Call<LessonListResponse> call,
                                   @NonNull Response<LessonListResponse> response) {
                if (!isAdded()) return;
                progressLoading.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess()) {
                    List<Lesson> lessons = response.body().getListLesson();
                    if (lessons != null && !lessons.isEmpty()) {
                        showLessons(lessons);
                    } else {
                        showEmptyState();
                    }
                } else {
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LessonListResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                progressLoading.setVisibility(View.GONE);
                Log.e(TAG, "Network error: " + t.getMessage());
                showEmptyState();
            }
        });
    }

    private void showLessons(List<Lesson> lessons) {
        rvLessons.setVisibility(View.VISIBLE);
        llEmptyState.setVisibility(View.GONE);
        tvTotalLessons.setText(String.valueOf(lessons.size()));

        adapter = new AdminLessonAdapter(lessons, this::showEditLessonDialog, this::showEditQuizDialog, this::showDeleteLessonConfirmation);
        rvLessons.setAdapter(adapter);
    }

    private void showDeleteLessonConfirmation(Lesson lesson, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Lesson")
                .setMessage("Are you sure you want to delete lesson: " + lesson.getTitle() + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteLesson(lesson, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteLesson(Lesson lesson, int position) {
        progressLoading.setVisibility(View.VISIBLE);
        adminApi.deleteLesson(lesson.getLessonID()).enqueue(new Callback<DeleteCourseResponse>() {
            @Override
            public void onResponse(@NonNull Call<DeleteCourseResponse> call, @NonNull Response<DeleteCourseResponse> response) {
                if (!isAdded()) return;
                progressLoading.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(getContext(), "Lesson deleted", Toast.LENGTH_SHORT).show();
                    fetchLessons();
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Unknown error";
                    Toast.makeText(getContext(), "Failed: " + msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeleteCourseResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                progressLoading.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEmptyState() {
        rvLessons.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.VISIBLE);
        tvTotalLessons.setText("0");
    }

    // ===== Edit Lesson Dialog =====
    private void showEditLessonDialog(Lesson lesson, int position) {
        if (!isAdded()) return;

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        addLabel(layout, "Title");
        EditText etTitle = new EditText(requireContext());
        etTitle.setHint("Enter lesson title");
        etTitle.setText(lesson.getTitle());
        layout.addView(etTitle);

        addLabel(layout, "Content Type");
        EditText etContentType = new EditText(requireContext());
        etContentType.setHint("reading / video / quiz");
        etContentType.setText(lesson.getContentType());
        layout.addView(etContentType);

        addLabel(layout, "Video URL");
        EditText etVideoUrl = new EditText(requireContext());
        etVideoUrl.setHint("https://... (optional)");
        etVideoUrl.setText(lesson.getVideoUrl());
        layout.addView(etVideoUrl);

        addLabel(layout, "Content");
        EditText etContent = new EditText(requireContext());
        etContent.setHint("Lesson content text (optional)");
        etContent.setText(lesson.getContent());
        etContent.setMinLines(3);
        layout.addView(etContent);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Lesson")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String contentType = etContentType.getText().toString().trim();
                    String videoUrl = etVideoUrl.getText().toString().trim();
                    String content = etContent.getText().toString().trim();

                    if (title.isEmpty()) {
                        Toast.makeText(getContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    UpdateLessonRequest request = new UpdateLessonRequest(
                            title,
                            contentType.isEmpty() ? null : contentType,
                            videoUrl.isEmpty() ? null : videoUrl,
                            content.isEmpty() ? null : content
                    );

                    adminApi.updateLesson(lesson.getLessonID(), request)
                            .enqueue(new Callback<UpdateLessonResponse>() {
                                @Override
                                public void onResponse(@NonNull Call<UpdateLessonResponse> call,
                                                       @NonNull Response<UpdateLessonResponse> response) {
                                    if (!isAdded()) return;
                                    if (response.isSuccessful() && response.body() != null
                                            && response.body().isSuccess()) {
                                        lesson.setTitle(title);
                                        lesson.setContentType(contentType.isEmpty() ? lesson.getContentType() : contentType);
                                        adapter.updateLesson(position, lesson);
                                        Toast.makeText(getContext(), "Lesson updated", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(), "Failed to update lesson", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<UpdateLessonResponse> call, @NonNull Throwable t) {
                                    if (!isAdded()) return;
                                    Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ===== Edit Quiz Dialog =====
    private void showEditQuizDialog(Lesson lesson) {
        if (!isAdded()) return;

        // First, fetch the quiz data
        quizApi.getQuiz(lesson.getLessonID()).enqueue(new Callback<QuizGetResponse>() {
            @Override
            public void onResponse(@NonNull Call<QuizGetResponse> call,
                                   @NonNull Response<QuizGetResponse> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess()) {
                    showQuizEditForm(response.body());
                } else {
                    Toast.makeText(getContext(), "No quiz found for this lesson", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<QuizGetResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Failed to load quiz", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showQuizEditForm(QuizGetResponse quiz) {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        // Question text
        TextView lblQuestion = new TextView(requireContext());
        lblQuestion.setText("Question:");
        lblQuestion.setTextColor(getResources().getColor(R.color.text_title, null));
        layout.addView(lblQuestion);

        EditText etQuestion = new EditText(requireContext());
        etQuestion.setText(quiz.getQuestionText());
        etQuestion.setMinLines(2);
        layout.addView(etQuestion);

        // Points
        TextView lblPoints = new TextView(requireContext());
        lblPoints.setText("Points:");
        lblPoints.setTextColor(getResources().getColor(R.color.text_title, null));
        layout.addView(lblPoints);

        EditText etPoints = new EditText(requireContext());
        etPoints.setText(String.valueOf(quiz.getPoints()));
        etPoints.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etPoints);

        // Options
        List<QuizGetResponse.OptionResponse> options = quiz.getOptions();
        EditText[] optionFields = new EditText[options != null ? options.size() : 0];

        if (options != null) {
            for (int i = 0; i < options.size(); i++) {
                TextView lblOpt = new TextView(requireContext());
                lblOpt.setText("Option " + (i + 1) + ":");
                lblOpt.setTextColor(getResources().getColor(R.color.text_secondary, null));
                layout.addView(lblOpt);

                EditText etOpt = new EditText(requireContext());
                etOpt.setText(options.get(i).getOptionText());
                layout.addView(etOpt);
                optionFields[i] = etOpt;
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Quiz (ID: " + quiz.getQuestionId() + ")")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String questionText = etQuestion.getText().toString().trim();
                    int points;
                    try {
                        points = Integer.parseInt(etPoints.getText().toString().trim());
                    } catch (NumberFormatException e) {
                        points = quiz.getPoints();
                    }

                    // Build option updates JSON manually via Gson/Retrofit
                    StringBuilder json = new StringBuilder();
                    json.append("{\"questionText\":\"").append(escapeJson(questionText)).append("\",");
                    json.append("\"points\":").append(points).append(",");
                    json.append("\"options\":[");

                    if (options != null) {
                        for (int i = 0; i < options.size(); i++) {
                            if (i > 0) json.append(",");
                            json.append("{\"optionId\":").append(options.get(i).getOptionId());
                            json.append(",\"optionText\":\"").append(escapeJson(optionFields[i].getText().toString().trim()));
                            json.append("\",\"isCorrect\":true}"); // preserve existing correctness
                        }
                    }
                    json.append("]}");

                    // Use a raw map approach via Retrofit
                    java.util.Map<String, Object> body = new java.util.HashMap<>();
                    body.put("questionText", questionText);
                    body.put("points", points);

                    java.util.List<java.util.Map<String, Object>> optList = new java.util.ArrayList<>();
                    if (options != null) {
                        for (int i = 0; i < options.size(); i++) {
                            java.util.Map<String, Object> opt = new java.util.HashMap<>();
                            opt.put("optionId", options.get(i).getOptionId());
                            opt.put("optionText", optionFields[i].getText().toString().trim());
                            opt.put("isCorrect", true); // TODO: let admin toggle
                            optList.add(opt);
                        }
                    }
                    body.put("options", optList);

                    // Call raw endpoint
                    adminApi.updateQuizRaw(quiz.getQuestionId(), body)
                            .enqueue(new Callback<UpdateLessonResponse>() {
                                @Override
                                public void onResponse(@NonNull Call<UpdateLessonResponse> call,
                                                       @NonNull Response<UpdateLessonResponse> r) {
                                    if (!isAdded()) return;
                                    if (r.isSuccessful()) {
                                        Toast.makeText(getContext(), "Quiz updated", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(), "Failed to update quiz", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<UpdateLessonResponse> call, @NonNull Throwable t) {
                                    if (!isAdded()) return;
                                    Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
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
