package com.eduflex.android.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminLessonsFragment extends Fragment {

    private static final String TAG = "AdminLessonsFragment";
    private RecyclerView rvLessons;
    private ProgressBar progressLoading;
    private View llEmptyState;
    private TextView tvTotalLessons, tvCourseName;
    private FloatingActionButton fabAddLesson;
    private AdminApi adminApi;
    private LessonApi lessonApi;
    private QuizApi quizApi;
    private AdminLessonAdapter adapter;
    private String courseId, courseTitle;

    public AdminLessonsFragment() { super(R.layout.fragment_admin_lessons); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        fabAddLesson = view.findViewById(R.id.fab_add_lesson);
        tvCourseName.setText(courseTitle);
        rvLessons.setLayoutManager(new LinearLayoutManager(getContext()));
        btnBack.setOnClickListener(v -> Navigation.findNavController(requireView()).navigateUp());
        fabAddLesson.setOnClickListener(v -> showAddLessonDialog());
    }

    private void fetchLessons() {
        progressLoading.setVisibility(View.VISIBLE);
        rvLessons.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.GONE);
        lessonApi.getLessons(courseId).enqueue(new Callback<LessonListResponse>() {
            @Override
            public void onResponse(@NonNull Call<LessonListResponse> call, @NonNull Response<LessonListResponse> response) {
                if (!isAdded()) return;
                progressLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Lesson> lessons = response.body().getListLesson();
                    if (lessons != null && !lessons.isEmpty()) showLessons(lessons);
                    else showEmptyState();
                } else showEmptyState();
            }
            @Override
            public void onFailure(@NonNull Call<LessonListResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                progressLoading.setVisibility(View.GONE);
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

    private void showEmptyState() {
        rvLessons.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.VISIBLE);
        tvTotalLessons.setText("0");
    }

    // ===== Add Lesson =====
    private void showAddLessonDialog() {
        if (!isAdded()) return;
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        addLabel(layout, "Title *");
        EditText etTitle = new EditText(requireContext());
        etTitle.setHint("Enter lesson title");
        layout.addView(etTitle);

        addLabel(layout, "Content Type *");
        Spinner spType = new Spinner(requireContext());
        String[] types = {"TEXT", "VIDEO", "QUIZ"};
        spType.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, types));
        layout.addView(spType);

        addLabel(layout, "Video URL");
        EditText etVideo = new EditText(requireContext());
        etVideo.setHint("https://... (for VIDEO type)");
        layout.addView(etVideo);

        addLabel(layout, "Content");
        EditText etContent = new EditText(requireContext());
        etContent.setHint("Lesson content (for TEXT type)");
        etContent.setMinLines(3);
        layout.addView(etContent);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add New Lesson")
                .setView(layout)
                .setPositiveButton("Create", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String type = spType.getSelectedItem().toString();
                    if (title.isEmpty()) {
                        Toast.makeText(getContext(), "Title is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Map<String, Object> body = new HashMap<>();
                    body.put("courseID", courseId);
                    body.put("title", title);
                    body.put("contentType", type);
                    String video = etVideo.getText().toString().trim();
                    String content = etContent.getText().toString().trim();
                    if (!video.isEmpty()) body.put("videoUrl", video);
                    if (!content.isEmpty()) body.put("content", content);

                    adminApi.createLesson(body).enqueue(new Callback<UpdateLessonResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<UpdateLessonResponse> call, @NonNull Response<UpdateLessonResponse> r) {
                            if (!isAdded()) return;
                            if (r.isSuccessful() && r.body() != null && r.body().isSuccess()) {
                                Toast.makeText(getContext(), "Lesson + Quiz created!", Toast.LENGTH_SHORT).show();
                                fetchLessons();
                            } else {
                                Toast.makeText(getContext(), "Failed to create lesson", Toast.LENGTH_SHORT).show();
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

    // ===== Delete Lesson =====
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
                    Toast.makeText(getContext(), "Failed to delete", Toast.LENGTH_SHORT).show();
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

    // ===== Edit Lesson Dialog =====
    private void showEditLessonDialog(Lesson lesson, int position) {
        if (!isAdded()) return;
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        addLabel(layout, "Title");
        EditText etTitle = new EditText(requireContext());
        etTitle.setText(lesson.getTitle());
        layout.addView(etTitle);

        addLabel(layout, "Content Type");
        Spinner spType = new Spinner(requireContext());
        String[] types = {"TEXT", "VIDEO", "QUIZ"};
        spType.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, types));
        String curType = lesson.getContentType();
        for (int i = 0; i < types.length; i++) {
            if (types[i].equalsIgnoreCase(curType)) { spType.setSelection(i); break; }
        }
        layout.addView(spType);

        addLabel(layout, "Video URL");
        EditText etVideo = new EditText(requireContext());
        etVideo.setText(lesson.getVideoUrl());
        layout.addView(etVideo);

        addLabel(layout, "Content");
        EditText etContent = new EditText(requireContext());
        etContent.setText(lesson.getContent());
        etContent.setMinLines(3);
        layout.addView(etContent);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Lesson: " + lesson.getTitle())
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    if (title.isEmpty()) {
                        Toast.makeText(getContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String ct = spType.getSelectedItem().toString();
                    String video = etVideo.getText().toString().trim();
                    String content = etContent.getText().toString().trim();

                    UpdateLessonRequest request = new UpdateLessonRequest(title,
                            ct.isEmpty() ? null : ct, video.isEmpty() ? null : video,
                            content.isEmpty() ? null : content, lesson.getParentLessonId());

                    adminApi.updateLesson(lesson.getLessonID(), request).enqueue(new Callback<UpdateLessonResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<UpdateLessonResponse> call, @NonNull Response<UpdateLessonResponse> r) {
                            if (!isAdded()) return;
                            if (r.isSuccessful() && r.body() != null && r.body().isSuccess()) {
                                lesson.setTitle(title);
                                if (!ct.isEmpty()) lesson.setContentType(ct);
                                adapter.updateLesson(position, lesson);
                                Toast.makeText(getContext(), "Lesson updated", Toast.LENGTH_SHORT).show();
                            } else Toast.makeText(getContext(), "Failed to update", Toast.LENGTH_SHORT).show();
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
        quizApi.getQuiz(lesson.getLessonID()).enqueue(new Callback<QuizGetResponse>() {
            @Override
            public void onResponse(@NonNull Call<QuizGetResponse> call, @NonNull Response<QuizGetResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()
                        && response.body().getQuestions() != null && !response.body().getQuestions().isEmpty()) {
                    showQuizEditForm(lesson, response.body().getQuestions());
                } else {
                    Toast.makeText(getContext(), "No quiz questions found. Add questions below.", Toast.LENGTH_SHORT).show();
                    showAddQuestionDialog(lesson);
                }
            }
            @Override
            public void onFailure(@NonNull Call<QuizGetResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Failed to load quiz", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showQuizEditForm(Lesson lesson, List<QuizGetResponse.QuestionResponse> questions) {
        ScrollView scrollView = new ScrollView(requireContext());
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);
        scrollView.addView(layout);

        EditText[] etQuestions = new EditText[questions.size()];
        EditText[] etPointsList = new EditText[questions.size()];
        EditText[][] optionFields = new EditText[questions.size()][];

        for (int qi = 0; qi < questions.size(); qi++) {
            QuizGetResponse.QuestionResponse q = questions.get(qi);
            final int questionIndex = qi;

            // Header row with delete button
            LinearLayout headerRow = new LinearLayout(requireContext());
            headerRow.setOrientation(LinearLayout.HORIZONTAL);
            headerRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
            headerRow.setPadding(0, qi == 0 ? 0 : 32, 0, 8);

            TextView lblHeader = new TextView(requireContext());
            lblHeader.setText("Question " + (qi + 1));
            lblHeader.setTextColor(getResources().getColor(R.color.text_title, null));
            lblHeader.setTypeface(null, android.graphics.Typeface.BOLD);
            lblHeader.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            headerRow.addView(lblHeader);

            // Delete question button
            TextView btnDelete = new TextView(requireContext());
            btnDelete.setText("🗑️ Delete");
            btnDelete.setTextColor(0xFFE53935);
            btnDelete.setPadding(16, 8, 16, 8);
            btnDelete.setOnClickListener(v -> confirmDeleteQuestion(q.getQuestionId(), lesson));
            headerRow.addView(btnDelete);
            layout.addView(headerRow);

            addLabel(layout, "Question text:");
            EditText etQ = new EditText(requireContext());
            etQ.setText(q.getQuestionText());
            etQ.setMinLines(2);
            layout.addView(etQ);
            etQuestions[qi] = etQ;

            addLabel(layout, "Points:");
            EditText etP = new EditText(requireContext());
            etP.setText(String.valueOf(q.getPoints()));
            etP.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            layout.addView(etP);
            etPointsList[qi] = etP;

            List<QuizGetResponse.OptionResponse> options = q.getOptions() != null ? q.getOptions() : new ArrayList<>();
            optionFields[qi] = new EditText[options.size()];
            for (int i = 0; i < options.size(); i++) {
                addLabel(layout, "Option " + (i + 1) + ":");
                EditText etOpt = new EditText(requireContext());
                etOpt.setText(options.get(i).getOptionText());
                layout.addView(etOpt);
                optionFields[qi][i] = etOpt;
            }
        }

        // Add Question button inside dialog
        TextView btnAddQ = new TextView(requireContext());
        btnAddQ.setText("➕ Add New Question");
        btnAddQ.setTextColor(getResources().getColor(R.color.cyan_primary, null));
        btnAddQ.setTypeface(null, android.graphics.Typeface.BOLD);
        btnAddQ.setPadding(0, 40, 0, 16);
        btnAddQ.setOnClickListener(v -> showAddQuestionDialog(lesson));
        layout.addView(btnAddQ);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Quiz (" + questions.size() + " question" + (questions.size() > 1 ? "s" : "") + ")")
                .setView(scrollView)
                .setPositiveButton("Save All", (dialog, which) -> {
                    for (int qi = 0; qi < questions.size(); qi++) {
                        QuizGetResponse.QuestionResponse q = questions.get(qi);
                        String qText = etQuestions[qi].getText().toString().trim();
                        int pts;
                        try { pts = Integer.parseInt(etPointsList[qi].getText().toString().trim()); }
                        catch (NumberFormatException e) { pts = q.getPoints(); }

                        List<QuizGetResponse.OptionResponse> options = q.getOptions() != null ? q.getOptions() : new ArrayList<>();
                        Map<String, Object> body = new HashMap<>();
                        body.put("questionText", qText);
                        body.put("points", pts);

                        List<Map<String, Object>> optList = new ArrayList<>();
                        for (int i = 0; i < options.size(); i++) {
                            Map<String, Object> opt = new HashMap<>();
                            opt.put("optionId", options.get(i).getOptionId());
                            opt.put("optionText", optionFields[qi][i].getText().toString().trim());
                            opt.put("isCorrect", i == 0);
                            optList.add(opt);
                        }
                        body.put("options", optList);

                        final int num = qi + 1, total = questions.size();
                        adminApi.updateQuizRaw(q.getQuestionId(), body).enqueue(new Callback<UpdateLessonResponse>() {
                            @Override
                            public void onResponse(@NonNull Call<UpdateLessonResponse> call, @NonNull Response<UpdateLessonResponse> r) {
                                if (!isAdded()) return;
                                if (num == total) Toast.makeText(getContext(), r.isSuccessful() ? "Quiz updated" : "Failed", Toast.LENGTH_SHORT).show();
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

    // ===== Add Question Dialog =====
    private void showAddQuestionDialog(Lesson lesson) {
        if (!isAdded()) return;
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        addLabel(layout, "Question Text *");
        EditText etQ = new EditText(requireContext());
        etQ.setHint("Enter question");
        etQ.setMinLines(2);
        layout.addView(etQ);

        addLabel(layout, "Points");
        EditText etPts = new EditText(requireContext());
        etPts.setText("10");
        etPts.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etPts);

        addLabel(layout, "Option A (correct answer) *");
        EditText etA = new EditText(requireContext());
        layout.addView(etA);

        addLabel(layout, "Option B");
        EditText etB = new EditText(requireContext());
        layout.addView(etB);

        addLabel(layout, "Option C");
        EditText etC = new EditText(requireContext());
        layout.addView(etC);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add Question to Quiz")
                .setView(layout)
                .setPositiveButton("Add", (dialog, which) -> {
                    String qText = etQ.getText().toString().trim();
                    String optA = etA.getText().toString().trim();
                    if (qText.isEmpty() || optA.isEmpty()) {
                        Toast.makeText(getContext(), "Question and Option A are required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int pts;
                    try { pts = Integer.parseInt(etPts.getText().toString().trim()); }
                    catch (NumberFormatException e) { pts = 10; }

                    List<Map<String, Object>> options = new ArrayList<>();
                    Map<String, Object> oA = new HashMap<>(); oA.put("optionText", optA); oA.put("isCorrect", true); options.add(oA);
                    String bText = etB.getText().toString().trim();
                    if (!bText.isEmpty()) { Map<String, Object> oB = new HashMap<>(); oB.put("optionText", bText); oB.put("isCorrect", false); options.add(oB); }
                    String cText = etC.getText().toString().trim();
                    if (!cText.isEmpty()) { Map<String, Object> oC = new HashMap<>(); oC.put("optionText", cText); oC.put("isCorrect", false); options.add(oC); }

                    Map<String, Object> body = new HashMap<>();
                    body.put("lessonId", lesson.getLessonID());
                    body.put("questionText", qText);
                    body.put("points", pts);
                    body.put("options", options);

                    adminApi.createQuiz(body).enqueue(new Callback<UpdateLessonResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<UpdateLessonResponse> call, @NonNull Response<UpdateLessonResponse> r) {
                            if (!isAdded()) return;
                            Toast.makeText(getContext(), r.isSuccessful() ? "Question added!" : "Failed", Toast.LENGTH_SHORT).show();
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

    // ===== Delete Question =====
    private void confirmDeleteQuestion(long questionId, Lesson lesson) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Question")
                .setMessage("Are you sure you want to delete this question?")
                .setPositiveButton("Delete", (d, w) -> {
                    adminApi.deleteQuestion(questionId).enqueue(new Callback<DeleteCourseResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<DeleteCourseResponse> call, @NonNull Response<DeleteCourseResponse> r) {
                            if (!isAdded()) return;
                            if (r.isSuccessful() && r.body() != null && r.body().isSuccess()) {
                                Toast.makeText(getContext(), "Question deleted", Toast.LENGTH_SHORT).show();
                                showEditQuizDialog(lesson); // Refresh
                            } else Toast.makeText(getContext(), "Failed to delete", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onFailure(@NonNull Call<DeleteCourseResponse> call, @NonNull Throwable t) {
                            if (!isAdded()) return;
                            Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
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
