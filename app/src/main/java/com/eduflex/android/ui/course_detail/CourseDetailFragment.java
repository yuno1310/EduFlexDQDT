package com.eduflex.android.ui.course_detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.eduflex.android.R;
import com.eduflex.android.adapter.LessonAdapter;
import com.eduflex.android.model.Lesson;
import java.util.Arrays;
import java.util.List;

public class CourseDetailFragment extends Fragment {

    private String courseId;
    private String courseTitle;
    private String courseDescription;

    public CourseDetailFragment() {
        super(R.layout.fragment_course_detail);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        // Setup lessons list
        RecyclerView rvLessons = view.findViewById(R.id.rv_lessons);
        rvLessons.setLayoutManager(new LinearLayoutManager(getContext()));
        
        List<Lesson> mockLessons = Arrays.asList(
                new Lesson("Introduction", "video"),
                new Lesson("Core Concepts", "reading"),
                new Lesson("Practice Quiz", "quiz"),
                new Lesson("Mini Project", "assignment"));
        
        rvLessons.setAdapter(new LessonAdapter(mockLessons));
    }
}
