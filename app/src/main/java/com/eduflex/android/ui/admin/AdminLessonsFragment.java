package com.eduflex.android.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
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

        addLabel(layout, "Parent Lesson (optional - for quiz lessons)");
        android.widget.Spinner spinnerParentLesson = new android.widget.Spinner(requireContext());
        layout.addView(spinnerParentLesson);

        // Populate parent lesson options (exclude current lesson to avoid self-reference)
        java.util.List<Lesson> allLessons = (adapter != null) ? adapter.getLessons() : new java.util.ArrayList<>();
        java.util.List<String> parentOptions = new java.util.ArrayList<>();
        parentOptions.add("None (quiz lesson)"); // null option
        int selectedPosition = 0; // default: no parent

        // Add existing lessons as options
        for (int i = 0; i < allLessons.size(); i++) {
            Lesson l = allLessons.get(i);
            // Skip current lesson and lessons with existing parent
            if (!l.getLessonID().equals(lesson.getLessonID()) && l.getParentLessonId() == null) {
                parentOptions.add(l.getTitle());
                if (lesson.getParentLessonId() != null && l.getLessonID().equals(lesson.getParentLessonId())) {
                    selectedPosition = i + 1; // +1 because index 0 is "None"
                }
            }
        }

        android.widget.ArrayAdapter<String> adapterSpinner = new android.widget.ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                parentOptions);
        spinnerParentLesson.setAdapter(adapterSpinner);
        spinnerParentLesson.setSelection(selectedPosition);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Lesson")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String contentType = etContentType.getText().toString().trim();
                    String videoUrl = etVideoUrl.getText().toString().trim();
                    String content = etContent.getText().toString().trim();

                    // Get selected parent lesson ID (use final array for lambda capture)
                    final String[] parentLessonIdRef = new String[1];
                    int selectedIdx = spinnerParentLesson.getSelectedItemPosition();
                    if (selectedIdx > 0) { // index 0 is "None"
                        Lesson selectedParent = allLessons.get(selectedIdx - 1);
                        parentLessonIdRef[0] = selectedParent.getLessonID();
                    } else {
                        parentLessonIdRef[0] = null;
                    }

                    if (title.isEmpty()) {
                        Toast.makeText(getContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    UpdateLessonRequest request = new UpdateLessonRequest(
                            title,
                            contentType.isEmpty() ? null : contentType,
                            videoUrl.isEmpty() ? null : videoUrl,
                            content.isEmpty() ? null : content,
                            parentLessonIdRef[0]
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
                                        lesson.setParentLessonId(parentLessonIdRef[0]);
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
                        && response.body().isSuccess()
                        && response.body().getQuestions() != null
                        && !response.body().getQuestions().isEmpty()) {
                    showQuizEditForm(response.body().getQuestions());
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

    private void showQuizEditForm(List<QuizGetResponse.QuestionResponse> questions) {
        ScrollView scrollView = new ScrollView(requireContext());
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);
        scrollView.addView(layout);

        // Per-question fields stored for save
        EditText[] etQuestions = new EditText[questions.size()];
        EditText[] etPointsList = new EditText[questions.size()];
        EditText[][] optionFields = new EditText[questions.size()][];

        for (int qi = 0; qi < questions.size(); qi++) {
            QuizGetResponse.QuestionResponse q = questions.get(qi);

            TextView lblHeader = new TextView(requireContext());
            lblHeader.setText("Question " + (qi + 1));
            lblHeader.setTextColor(getResources().getColor(R.color.text_title, null));
            lblHeader.setTypeface(null, android.graphics.Typeface.BOLD);
            lblHeader.setPadding(0, qi == 0 ? 0 : 32, 0, 8);
            layout.addView(lblHeader);

            TextView lblQuestion = new TextView(requireContext());
            lblQuestion.setText("Question text:");
            lblQuestion.setTextColor(getResources().getColor(R.color.text_secondary, null));
            layout.addView(lblQuestion);

            EditText etQuestion = new EditText(requireContext());
            etQuestion.setText(q.getQuestionText());
            etQuestion.setMinLines(2);
            layout.addView(etQuestion);
            etQuestions[qi] = etQuestion;

            TextView lblPoints = new TextView(requireContext());
            lblPoints.setText("Points:");
            lblPoints.setTextColor(getResources().getColor(R.color.text_secondary, null));
            layout.addView(lblPoints);

            EditText etPoints = new EditText(requireContext());
            etPoints.setText(String.valueOf(q.getPoints()));
            etPoints.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            layout.addView(etPoints);
            etPointsList[qi] = etPoints;

            List<QuizGetResponse.OptionResponse> options = q.getOptions() != null ? q.getOptions() : new java.util.ArrayList<>();
            optionFields[qi] = new EditText[options.size()];
            for (int i = 0; i < options.size(); i++) {
                TextView lblOpt = new TextView(requireContext());
                lblOpt.setText("Option " + (i + 1) + ":");
                lblOpt.setTextColor(getResources().getColor(R.color.text_secondary, null));
                layout.addView(lblOpt);

                EditText etOpt = new EditText(requireContext());
                etOpt.setText(options.get(i).getOptionText());
                layout.addView(etOpt);
                optionFields[qi][i] = etOpt;
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Quiz (" + questions.size() + " question" + (questions.size() > 1 ? "s" : "") + ")")
                .setView(scrollView)
                .setPositiveButton("Save All", (dialog, which) -> {
                    for (int qi = 0; qi < questions.size(); qi++) {
                        QuizGetResponse.QuestionResponse q = questions.get(qi);
                        String questionText = etQuestions[qi].getText().toString().trim();
                        int points;
                        try {
                            points = Integer.parseInt(etPointsList[qi].getText().toString().trim());
                        } catch (NumberFormatException e) {
                            points = q.getPoints();
                        }

                        List<QuizGetResponse.OptionResponse> options = q.getOptions() != null ? q.getOptions() : new java.util.ArrayList<>();
                        java.util.Map<String, Object> body = new java.util.HashMap<>();
                        body.put("questionText", questionText);
                        body.put("points", points);

                        java.util.List<java.util.Map<String, Object>> optList = new java.util.ArrayList<>();
                        for (int i = 0; i < options.size(); i++) {
                            java.util.Map<String, Object> opt = new java.util.HashMap<>();
                            opt.put("optionId", options.get(i).getOptionId());
                            opt.put("optionText", optionFields[qi][i].getText().toString().trim());
                            opt.put("isCorrect", true);
                            optList.add(opt);
                        }
                        body.put("options", optList);

                        final int questionNumber = qi + 1;
                        final int total = questions.size();
                        adminApi.updateQuizRaw(q.getQuestionId(), body)
                                .enqueue(new Callback<UpdateLessonResponse>() {
                                    @Override
                                    public void onResponse(@NonNull Call<UpdateLessonResponse> call,
                                                           @NonNull Response<UpdateLessonResponse> r) {
                                        if (!isAdded()) return;
                                        if (questionNumber == total) {
                                            Toast.makeText(getContext(),
                                                    r.isSuccessful() ? "Quiz updated" : "Failed to update quiz",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(@NonNull Call<UpdateLessonResponse> call, @NonNull Throwable t) {
                                        if (!isAdded()) return;
                                        Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
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
