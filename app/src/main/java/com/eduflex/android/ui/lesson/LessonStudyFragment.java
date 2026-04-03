package com.eduflex.android.ui.lesson;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.eduflex.android.R;

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

        TextView tvTitle = view.findViewById(R.id.tv_lesson_study_title);
        TextView tvType = view.findViewById(R.id.tv_lesson_study_type);
        TextView tvTextContent = view.findViewById(R.id.tv_lesson_text_content);
        ImageView ivVideoPlaceholder = view.findViewById(R.id.iv_video_placeholder);

        view.findViewById(R.id.btn_back_lesson).setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.popBackStack();
        });

        tvTitle.setText(lessonTitle);
        tvType.setText(contentType.toUpperCase());

        String normalizedType = contentType.toLowerCase();
        if ("video".equals(normalizedType)) {
            ivVideoPlaceholder.setVisibility(View.VISIBLE);
            tvTextContent.setVisibility(View.GONE);
        } else {
            ivVideoPlaceholder.setVisibility(View.GONE);
            tvTextContent.setVisibility(View.VISIBLE);
            tvTextContent.setText(lessonContent.isEmpty() ? "No text content available." : lessonContent);
        }
    }
}
