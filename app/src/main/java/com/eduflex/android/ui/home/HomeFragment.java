package com.eduflex.android.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private TextView tvStreak;
    private TextView tvXp;
    private TextView tvLevel;
    private GamificationApi gamificationApi;
    private TokenManager tokenManager;

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
                "Data Structures");
    }

    private List<String> getMockCategories() {
        return Arrays.asList(
                "Programming", "Design", "Business", "Marketing", "Data Science");
    }
}