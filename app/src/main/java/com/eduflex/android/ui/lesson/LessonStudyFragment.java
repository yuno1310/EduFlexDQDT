package com.eduflex.android.ui.lesson;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
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
        String lessonTitle = args != null ? args.getString("lessonTitle", "Untitled Lesson") : "Untitled Lesson";
        String contentType = args != null ? args.getString("contentType", "reading") : "reading";
        String lessonContent = args != null ? args.getString("lessonContent", "") : "";
        int lessonIndex = args != null ? args.getInt("lessonIndex", 0) : 0;
        List<Lesson> lessonList = getLessonList(args);

        TextView tvTitle = view.findViewById(R.id.tv_lesson_study_title);
        TextView tvType = view.findViewById(R.id.tv_lesson_study_type);
        TextView tvTextContent = view.findViewById(R.id.tv_lesson_text_content);
        ImageView ivVideoPlaceholder = view.findViewById(R.id.iv_video_placeholder);
        Button btnPrev = view.findViewById(R.id.btn_prev_lesson);
        Button btnNext = view.findViewById(R.id.btn_next_lesson);

        view.findViewById(R.id.btn_back_lesson).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());

        tvTitle.setText(lessonTitle);
        tvType.setText(contentType.toUpperCase());

        if ("video".equals(contentType.toLowerCase())) {
            ivVideoPlaceholder.setVisibility(View.VISIBLE);
            tvTextContent.setVisibility(View.GONE);
        } else {
            ivVideoPlaceholder.setVisibility(View.GONE);
            tvTextContent.setVisibility(View.VISIBLE);
            tvTextContent.setText(lessonContent.isEmpty() ? "No text content available." : lessonContent);
        }

        btnPrev.setEnabled(lessonIndex > 0);
        btnNext.setEnabled(lessonIndex < lessonList.size() - 1);

        btnPrev.setOnClickListener(v -> navigateToLesson(lessonIndex - 1, lessonList, args));
        btnNext.setOnClickListener(v -> navigateToLesson(lessonIndex + 1, lessonList, args));
    }

    @SuppressWarnings("unchecked")
    private List<Lesson> getLessonList(Bundle args) {
        if (args == null) return new ArrayList<>();
        Object raw = args.getSerializable("lessonList");
        if (raw instanceof ArrayList) return (ArrayList<Lesson>) raw;
        return new ArrayList<>();
    }

    private void navigateToLesson(int index, List<Lesson> lessonList, Bundle originalArgs) {
        if (index < 0 || index >= lessonList.size()) return;
        Lesson lesson = lessonList.get(index);

        String courseId = originalArgs != null ? originalArgs.getString("courseId", "") : "";
        int sourceTab = originalArgs != null ? originalArgs.getInt("sourceTab", 0) : 0;
        String type = lesson.getContentType() == null ? "" : lesson.getContentType().toLowerCase();

        Bundle args = new Bundle();
        args.putString("lessonId", lesson.getLessonID());
        args.putString("lessonTitle", lesson.getTitle());
        args.putString("courseId", courseId);
        args.putString("contentType", lesson.getContentType());
        args.putInt("sourceTab", sourceTab);
        args.putInt("lessonIndex", index);
        args.putSerializable("lessonList", new ArrayList<>(lessonList));

        NavController nav = NavHostFragment.findNavController(this);

        if ("quiz_fill_blank".equals(type) || "quiz_dien_tu".equals(type)
                || "quiz_new".equals(type) || "quiz_new_type".equals(type)) {
            nav.navigate(R.id.fillBlankQuizFragment, args);
        } else if ("quiz".equals(type)) {
            nav.navigate(R.id.quizFragment, args);
        } else {
            args.putString("lessonContent", getMockContent(lesson.getTitle(), type));
            nav.navigate(R.id.lessonStudyFragment, args);
        }
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
