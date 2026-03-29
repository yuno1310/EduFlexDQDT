package com.eduflex.android.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eduflex.android.R;
import com.eduflex.android.adapter.CategoryAdapter;
import com.eduflex.android.adapter.ContinueLearningAdapter;
import com.eduflex.android.adapter.CourseCardAdapter;
import com.eduflex.android.api.ApiClient;
import com.eduflex.android.api.GamificationApi;
import com.eduflex.android.auth.TokenManager;
import com.eduflex.android.model.GamificationStatsResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.eduflex.android.api.CourseApi;
import com.eduflex.android.model.CategoryListResponse;
import com.eduflex.android.model.Course;
import com.eduflex.android.model.CourseListResponse;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private TextView tvStreak;
    private TextView tvXp;
    private TextView tvLevel;
    private GamificationApi gamificationApi;
    private TokenManager tokenManager;
    private CourseApi courseApi;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind banner views
        tvStreak = view.findViewById(R.id.tv_streak);
        tvXp = view.findViewById(R.id.tv_xp);
        tvLevel = view.findViewById(R.id.tv_level);

        // Init API (use authenticated client so the JWT is attached)
        gamificationApi = ApiClient.createAuthenticatedService(GamificationApi.class);
        tokenManager = new TokenManager(requireContext());
        courseApi = ApiClient.createAuthenticatedService(CourseApi.class);

        // Setup UI
        setupContinueLearning(view);
        setupFeaturedCourses(view);
        setupCategories(view);

        // Fetch gamification data
        fetchGamificationStats();
    }

    // ── Gamification API ──

    private void fetchGamificationStats() {
        String userId = tokenManager.getUserId();
        if (userId == null) {
            Log.e(TAG, "No user ID available from token");
            showFallbackBanner();
            return;
        }
        gamificationApi.getStats(userId).enqueue(new Callback<GamificationStatsResponse>() {
            @Override
            public void onResponse(@NonNull Call<GamificationStatsResponse> call,
                    @NonNull Response<GamificationStatsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateBanner(response.body());
                } else {
                    Log.e(TAG, "Failed to load stats: " + response.code());
                    showFallbackBanner();
                }
            }

            @Override
            public void onFailure(@NonNull Call<GamificationStatsResponse> call,
                    @NonNull Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                showFallbackBanner();
            }
        });
    }

    private void updateBanner(GamificationStatsResponse stats) {
        if (!isAdded())
            return;

        int streak = stats.getStreakDays();
        int xp = stats.getXp();
        int level = stats.getLevel();

        tvStreak.setText(streak > 0
                ? "🔥 " + streak + "-day streak!"
                : "🔥 Start your streak today!");
        tvXp.setText("You have " + xp + " XP — keep it up!");
        tvLevel.setText("Lv." + level);
    }

    private void showFallbackBanner() {
        if (!isAdded())
            return;
        tvStreak.setText("🔥 Welcome!");
        tvXp.setText("Connect to see your XP");
        tvLevel.setText("Lv.–");
    }

    // ── RecyclerView setup ──

    private void setupContinueLearning(View view) {
        RecyclerView rv = view.findViewById(R.id.rv_continue_learning);
        rv.setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false));

        courseApi.getCourses().enqueue(new Callback<CourseListResponse>() {
            @Override
            public void onResponse(Call<CourseListResponse> call, retrofit2.Response<CourseListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CourseListResponse body = response.body();
                    if (body.getListCourse() != null) {
                        rv.setAdapter(new ContinueLearningAdapter(body.getListCourse(), HomeFragment.this::onCourseClick));
                    }
                } else {
                    Log.e(TAG, "Failed to load continue learning courses: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CourseListResponse> call, Throwable t) {
                Log.e(TAG, "Network error loading continue learning courses: " + t.getMessage());
            }
        });
    }

    private void setupFeaturedCourses(View view) {
        RecyclerView rv = view.findViewById(R.id.rv_featured_courses);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        courseApi.getCourses().enqueue(new Callback<CourseListResponse>() {
            @Override
            public void onResponse(Call<CourseListResponse> call, retrofit2.Response<CourseListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CourseListResponse body = response.body();
                    if (body.getListCourse() != null) {
                        rv.setAdapter(new CourseCardAdapter(body.getListCourse(), HomeFragment.this::onCourseClick));
                    }
                } else {
                    Log.e(TAG, "Failed to load featured courses: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CourseListResponse> call, Throwable t) {
                Log.e(TAG, "Network error loading featured courses: " + t.getMessage());
            }
        });
    }

    private void setupCategories(View view) {
        RecyclerView rv = view.findViewById(R.id.rv_categories);
        rv.setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false));

        courseApi.getCategories().enqueue(new Callback<CategoryListResponse>() {
            @Override
            public void onResponse(Call<CategoryListResponse> call, Response<CategoryListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CategoryListResponse body = response.body();
                    if (body.getListCategory() != null) {
                        rv.setAdapter(new CategoryAdapter(body.getListCategory()));
                    }
                } else {
                    Log.e(TAG, "Failed to load categories: " + response.code());
                    // Optionally show fallback UI or error message
                }
            }

            @Override
            public void onFailure(Call<CategoryListResponse> call, Throwable t) {
                Log.e(TAG, "Network error loading categories: " + t.getMessage());
                // Optionally show fallback UI or error message
            }
        });
    }

    private void onCourseClick(Course course) {
        String courseTitle = course.getTitle() != null ? course.getTitle() : "Untitled Course";
        String description = "Learn about " + courseTitle + " with hands-on projects and interactive lessons.";
        
        Bundle args = new Bundle();
        args.putString("courseId", course.getCourseID());
        args.putString("courseTitle", courseTitle);
        args.putString("courseDescription", description);
        
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.courseDetailFragment, args);
    }
}