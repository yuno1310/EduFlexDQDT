package com.eduflex.android.ui.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.eduflex.android.R;
import com.eduflex.android.adapter.CourseCardAdapter;
import com.eduflex.android.model.Course;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchFragment extends Fragment {

    private static final List<Course> MOCK_COURSES = Arrays.asList(
            new Course("1", "Introduction to Java Programming", "Online", "Available"),
            new Course("2", "Android Development with Kotlin", "Online", "Available"),
            new Course("3", "Data Structures & Algorithms", "Offline", "Available"),
            new Course("4", "Web Development with React", "Online", "Available"),
            new Course("5", "Machine Learning Fundamentals", "Online", "Available"),
            new Course("6", "Database Design with PostgreSQL", "Offline", "Available"),
            new Course("7", "UI/UX Design Principles", "Online", "Available"),
            new Course("8", "Python for Beginners", "Online", "Available"),
            new Course("9", "Spring Boot REST APIs", "Online", "Available"),
            new Course("10", "DevOps and CI/CD Pipelines", "Offline", "Available")
    );

    private List<Course> filteredCourses = new ArrayList<>();
    private CourseCardAdapter adapter;

    public SearchFragment() {
        super(R.layout.fragment_search);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText etSearch = view.findViewById(R.id.et_search);
        TextView tvEmpty = view.findViewById(R.id.tv_search_empty);
        RecyclerView rvResults = view.findViewById(R.id.rv_search_results);

        rvResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CourseCardAdapter(filteredCourses, course -> openCourseDetail(course));
        rvResults.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim().toLowerCase();
                filteredCourses.clear();

                if (query.isEmpty()) {
                    rvResults.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.GONE);
                } else {
                    for (Course c : MOCK_COURSES) {
                        if (c.getTitle().toLowerCase().contains(query)) {
                            filteredCourses.add(c);
                        }
                    }
                    if (filteredCourses.isEmpty()) {
                        rvResults.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        rvResults.setVisibility(View.VISIBLE);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void openCourseDetail(Course course) {
        Bundle args = new Bundle();
        args.putString("courseId", course.getCourseID());
        args.putString("courseTitle", course.getTitle());
        args.putString("courseDescription", course.getLearningMode());
        NavController nav = NavHostFragment.findNavController(this);
        nav.navigate(R.id.courseDetailFragment, args);
    }
}
