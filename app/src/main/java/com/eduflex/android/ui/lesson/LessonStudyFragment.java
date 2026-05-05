package com.eduflex.android.ui.lesson;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import com.eduflex.android.R;
import com.eduflex.android.model.Lesson;

import java.util.ArrayList;
import java.util.List;

public class LessonStudyFragment extends Fragment {

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
        String quizLessonId = args != null ? args.getString("quizLessonId", null) : null;
        int lessonIndex = args != null ? args.getInt("lessonIndex", 0) : 0;
        List<Lesson> lessonList = getLessonList(args, "lessonList");
        List<Lesson> allLessonList = getLessonList(args, "allLessonList");
        if (allLessonList.isEmpty()) allLessonList = lessonList;

        TextView tvTitle = view.findViewById(R.id.tv_lesson_study_title);
        TextView tvType = view.findViewById(R.id.tv_lesson_study_type);
        TextView tvTextContent = view.findViewById(R.id.tv_lesson_text_content);
        ImageView ivVideoPlaceholder = view.findViewById(R.id.iv_video_placeholder);
        Button btnPrev = view.findViewById(R.id.btn_prev_lesson);
        Button btnNext = view.findViewById(R.id.btn_next_lesson);

        view.findViewById(R.id.btn_back_lesson).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack(R.id.courseDetailFragment, false));

        tvTitle.setText(lessonTitle);
        tvType.setText(contentType.toUpperCase());

        if ("video".equals(contentType.toLowerCase())) {
            ivVideoPlaceholder.setVisibility(View.VISIBLE);
            tvTextContent.setVisibility(View.GONE);
        } else {
            ivVideoPlaceholder.setVisibility(View.GONE);
            tvTextContent.setVisibility(View.VISIBLE);
            String display = lessonContent.isEmpty() ? "No text content available." : lessonContent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tvTextContent.setText(Html.fromHtml(display, Html.FROM_HTML_MODE_COMPACT));
            } else {
                tvTextContent.setText(Html.fromHtml(display));
            }
        }

        // Next is enabled if there's a quiz for this lesson OR there's a next content lesson
        boolean hasNextContent = lessonIndex < lessonList.size() - 1;
        boolean hasQuiz = quizLessonId != null && !quizLessonId.isEmpty();
        btnPrev.setEnabled(lessonIndex > 0);
        btnNext.setEnabled(hasNextContent || hasQuiz);

        final List<Lesson> finalAllLessonList = allLessonList;
        btnPrev.setOnClickListener(v -> navigateToLesson(lessonIndex - 1, lessonList, finalAllLessonList, args));
        btnNext.setOnClickListener(v -> {
            if (hasQuiz) {
                navigateToQuiz(quizLessonId, lessonId, lessonTitle, lessonIndex, lessonList, finalAllLessonList, args);
            } else {
                navigateToLesson(lessonIndex + 1, lessonList, finalAllLessonList, args);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private List<Lesson> getLessonList(Bundle args, String key) {
        if (args == null) return new ArrayList<>();
        Object raw = args.getSerializable(key);
        if (raw instanceof ArrayList) return (ArrayList<Lesson>) raw;
        return new ArrayList<>();
    }

    private void navigateToQuiz(String quizLessonId, String parentLessonId, String lessonTitle,
                                int lessonIndex, List<Lesson> lessonList, List<Lesson> allLessonList,
                                Bundle originalArgs) {
        String courseId = originalArgs != null ? originalArgs.getString("courseId", "") : "";
        int sourceTab = originalArgs != null ? originalArgs.getInt("sourceTab", 0) : 0;

        Bundle args = new Bundle();
        args.putString("lessonId", quizLessonId);
        args.putString("parentLessonId", parentLessonId);
        args.putString("lessonTitle", lessonTitle);
        args.putString("courseId", courseId);
        args.putInt("sourceTab", sourceTab);
        args.putInt("lessonIndex", lessonIndex);
        args.putSerializable("lessonList", new ArrayList<>(lessonList));
        args.putSerializable("allLessonList", new ArrayList<>(allLessonList));

        NavController nav = NavHostFragment.findNavController(this);
        nav.navigate(R.id.quizFragment, args);
    }

    private void navigateToLesson(int index, List<Lesson> lessonList, List<Lesson> allLessonList, Bundle originalArgs) {
        if (index < 0 || index >= lessonList.size()) return;
        Lesson lesson = lessonList.get(index);

        String courseId = originalArgs != null ? originalArgs.getString("courseId", "") : "";
        int sourceTab = originalArgs != null ? originalArgs.getInt("sourceTab", 0) : 0;

        Bundle args = new Bundle();
        args.putString("lessonId", lesson.getLessonID());
        args.putString("lessonTitle", lesson.getTitle());
        args.putString("courseId", courseId);
        args.putString("contentType", lesson.getContentType());
        args.putInt("sourceTab", sourceTab);
        args.putInt("lessonIndex", index);
        args.putSerializable("lessonList", new ArrayList<>(lessonList));
        args.putSerializable("allLessonList", new ArrayList<>(allLessonList));

        String quizLessonId = findQuizLessonId(lesson.getLessonID(), allLessonList);
        if (quizLessonId != null) args.putString("quizLessonId", quizLessonId);

        String content = lesson.getContent();
        args.putString("lessonContent", (content != null && !content.isEmpty()) ? content : "");

        NavController nav = NavHostFragment.findNavController(this);
        NavOptions replaceOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.lessonStudyFragment, true)
                .build();
        nav.navigate(R.id.lessonStudyFragment, args, replaceOptions);
    }

    private String findQuizLessonId(String parentId, List<Lesson> allLessons) {
        for (Lesson l : allLessons) {
            if (parentId.equals(l.getParentLessonId())) return l.getLessonID();
        }
        return null;
    }
}
