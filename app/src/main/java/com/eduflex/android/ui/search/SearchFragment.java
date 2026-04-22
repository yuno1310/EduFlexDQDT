package com.eduflex.android.ui.search;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.eduflex.android.api.ApiClient;
import com.eduflex.android.api.CourseApi;
import com.eduflex.android.model.Course;
import com.eduflex.android.model.CourseListResponse;
import com.eduflex.android.model.CourseSearchResult;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private static final long DEBOUNCE_MS = 300;

    private List<Course> displayedCourses = new ArrayList<>();
    private CourseCardAdapter adapter;
    private CourseApi courseApi;
    private Call<List<CourseSearchResult>> pendingCall;
    private Call<CourseListResponse> allCoursesCall;
    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearch;
    private RecyclerView rvResults;
    private TextView tvEmpty;

    public SearchFragment() {
        super(R.layout.fragment_search);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        courseApi = ApiClient.createAuthenticatedService(CourseApi.class);

        EditText etSearch = view.findViewById(R.id.et_search);
        tvEmpty = view.findViewById(R.id.tv_search_empty);
        rvResults = view.findViewById(R.id.rv_search_results);

        rvResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CourseCardAdapter(displayedCourses, course -> openCourseDetail(course));
        rvResults.setAdapter(adapter);

        loadAllCourses();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();

                if (query.isEmpty()) {
                    debounceHandler.removeCallbacks(pendingSearch);
                    cancelPending();
                    loadAllCourses();
                    return;
                }

                debounceHandler.removeCallbacks(pendingSearch);
                pendingSearch = () -> searchCourses(query);
                debounceHandler.postDelayed(pendingSearch, DEBOUNCE_MS);
            }
        });
    }

    private void loadAllCourses() {
        if (allCoursesCall != null) allCoursesCall.cancel();
        allCoursesCall = courseApi.getCourses();
        allCoursesCall.enqueue(new Callback<CourseListResponse>() {
            @Override
            public void onResponse(Call<CourseListResponse> call, Response<CourseListResponse> response) {
                if (!isAdded()) return;
                displayedCourses.clear();
                if (response.isSuccessful() && response.body() != null && response.body().getListCourse() != null) {
                    displayedCourses.addAll(response.body().getListCourse());
                }
                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(View.GONE);
                rvResults.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<CourseListResponse> call, Throwable t) {
                if (!isAdded() || call.isCanceled()) return;
            }
        });
    }

    private void searchCourses(String keyword) {
        cancelPending();

        pendingCall = courseApi.searchCourses(keyword);
        pendingCall.enqueue(new Callback<List<CourseSearchResult>>() {
            @Override
            public void onResponse(Call<List<CourseSearchResult>> call, Response<List<CourseSearchResult>> response) {
                if (!isAdded()) return;
                displayedCourses.clear();
                if (response.isSuccessful() && response.body() != null) {
                    for (CourseSearchResult result : response.body()) {
                        Course c = new Course(result.getCourseId(), result.getTitle(), "", "Available");
                        c.setImageUrl(result.getImageUrl());
                        displayedCourses.add(c);
                    }
                }
                adapter.notifyDataSetChanged();
                if (displayedCourses.isEmpty()) {
                    rvResults.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    rvResults.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<CourseSearchResult>> call, Throwable t) {
                if (!isAdded() || call.isCanceled()) return;
                displayedCourses.clear();
                adapter.notifyDataSetChanged();
                rvResults.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Search failed. Check your connection.");
            }
        });
    }

    private void cancelPending() {
        if (pendingCall != null) {
            pendingCall.cancel();
            pendingCall = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        debounceHandler.removeCallbacks(pendingSearch);
        cancelPending();
        if (allCoursesCall != null) allCoursesCall.cancel();
    }

    private void openCourseDetail(Course course) {
        Bundle args = new Bundle();
        args.putString("courseId", course.getCourseID());
        args.putString("courseTitle", course.getTitle());
        args.putString("courseDescription", course.getLearningMode());
        args.putString("imageUrl", course.getImageUrl());
        NavController nav = NavHostFragment.findNavController(this);
        nav.navigate(R.id.courseDetailFragment, args);
    }
}
