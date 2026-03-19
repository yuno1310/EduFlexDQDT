package com.eduflex.android.ui.home;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.eduflex.android.R;
import com.eduflex.android.adapter.ContinueLearningAdapter;
import com.eduflex.android.adapter.CourseCardAdapter;
import com.eduflex.android.adapter.CategoryAdapter;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {

    public HomeFragment() { super(R.layout.fragment_home); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupContinueLearning(view);
        setupFeaturedCourses(view);
        setupCategories(view);
    }

    private void setupContinueLearning(View view) {
        RecyclerView rv = view.findViewById(R.id.rv_continue_learning);
        rv.setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false));
        rv.setAdapter(new ContinueLearningAdapter(getMockCourses()));
    }

    private void setupFeaturedCourses(View view) {
        RecyclerView rv = view.findViewById(R.id.rv_featured_courses);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new CourseCardAdapter(getMockCourses()));
    }

    private void setupCategories(View view) {
        RecyclerView rv = view.findViewById(R.id.rv_categories);
        rv.setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false));
        rv.setAdapter(new CategoryAdapter(getMockCategories()));
    }

    private List<String> getMockCourses() {
        return Arrays.asList(
                "Introduction to Java",
                "Android Development",
                "Spring Boot Basics",
                "Data Structures"
        );
    }

    private List<String> getMockCategories() {
        return Arrays.asList(
                "Programming", "Design", "Business", "Marketing", "Data Science"
        );
    }
}