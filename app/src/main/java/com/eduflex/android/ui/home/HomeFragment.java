package com.eduflex.android.ui.home;

import android.os.Bundle;
import android.content.res.ColorStateList;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.drawable.GradientDrawable;

import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

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

    private ImageView ivFireIcon;
    private TextView tvStreak;
    private TextView tvXp;
    private TextView tvLevel;
    private LinearLayout llStreakDays;
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
        ivFireIcon = view.findViewById(R.id.iv_fire_icon);
        tvStreak = view.findViewById(R.id.tv_streak);
        tvXp = view.findViewById(R.id.tv_xp);
        tvLevel = view.findViewById(R.id.tv_level);
        llStreakDays = view.findViewById(R.id.ll_streak_days);

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
        String lastStudyDate = stats.getLastStudyDate();

        // Check if user studied today → orange fire, else → gray fire
        String today = java.time.LocalDate.now().toString(); // "2026-03-31"
        boolean studiedToday = today.equals(lastStudyDate);

        if (studiedToday) {
            setFireIconColor(R.color.fire_active);
        } else {
            setFireIconColor(R.color.fire_inactive);
        }

        tvStreak.setText(streak > 0
            ? streak + " day streak"
            : "Start your streak today");
        tvXp.setText("XP: " + xp + " | Keep going");
        tvLevel.setText("Lv." + level);
        renderStreakDays(streak, studiedToday);
    }

    private void showFallbackBanner() {
        if (!isAdded())
            return;
        setFireIconColor(R.color.fire_inactive);
        tvStreak.setText("Welcome!");
        tvXp.setText("Connect to see your XP");
        tvLevel.setText("Lv.–");
        renderStreakDays(0, false);
    }

    private void setFireIconColor(int colorResId) {
        int color = ContextCompat.getColor(requireContext(), colorResId);
        ImageViewCompat.setImageTintList(ivFireIcon, ColorStateList.valueOf(color));
    }

    private void renderStreakDays(int streakDays, boolean studiedToday) {
        if (llStreakDays == null || !isAdded()) {
            return;
        }

        llStreakDays.removeAllViews();
        int activeDays = studiedToday ? Math.min(streakDays, 7) : Math.max(0, Math.min(streakDays - 1, 7));

        for (int i = 0; i < 7; i++) {
            View cell = new View(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(14), dp(14));
            if (i < 6) {
                params.rightMargin = dp(6);
            }
            cell.setLayoutParams(params);

            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setCornerRadius(dp(4));
            bg.setColor(ContextCompat.getColor(requireContext(), i < activeDays ? R.color.fire_active : R.color.fire_inactive));
            cell.setBackground(bg);

            llStreakDays.addView(cell);
        }
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics());
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