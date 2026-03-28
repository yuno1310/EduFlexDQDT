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
import com.eduflex.android.model.GamificationStatsResponse;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    // TODO: Replace with actual logged-in user ID from auth
    private static final String MOCK_USER_ID = "48f57b58-f050-4655-abc6-09c60bdde7d7";

    private TextView tvStreak;
    private TextView tvXp;
    private TextView tvLevel;
    private GamificationApi gamificationApi;

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

        // Init API
        gamificationApi = ApiClient.createService(GamificationApi.class);

        // Setup UI
        setupContinueLearning(view);
        setupFeaturedCourses(view);
        setupCategories(view);

        // Fetch gamification data
        fetchGamificationStats();
    }

    // ── Gamification API ──

    private void fetchGamificationStats() {
        gamificationApi.getStats(MOCK_USER_ID).enqueue(new Callback<GamificationStatsResponse>() {
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