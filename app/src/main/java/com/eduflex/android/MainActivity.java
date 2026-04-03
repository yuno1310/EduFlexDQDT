package com.eduflex.android;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;
import android.view.View;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import com.eduflex.android.api.ApiClient;
import com.eduflex.android.api.GamificationApi;
import com.eduflex.android.auth.TokenManager;
import com.eduflex.android.model.AddXpRequest;
import com.eduflex.android.model.GamificationStatsResponse;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int DAILY_LOGIN_XP = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);
        applyEdgeToEdgeInsets();

        // Initialise API client so the auth interceptor can read the stored JWT
        ApiClient.init(this);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);
        syncBottomNavSelection(bottomNav, navController);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        Log.d("FCM_TOKEN", "Token: " + token);
                    }
                });

        // Award daily XP (once per day, covers both auto-login and manual login)
        awardDailyXpIfNeeded();
    }

    private void applyEdgeToEdgeInsets() {
        View root = findViewById(R.id.root_main);
        View navHost = findViewById(R.id.nav_host_fragment);
        View bottomNav = findViewById(R.id.bottom_nav);

        final int navHostTopPadding = navHost.getPaddingTop();
        final int bottomNavBottomPadding = bottomNav.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            navHost.setPadding(
                navHost.getPaddingLeft(),
                navHostTopPadding + systemBars.top,
                navHost.getPaddingRight(),
                navHost.getPaddingBottom()
            );

            bottomNav.setPadding(
                bottomNav.getPaddingLeft(),
                bottomNav.getPaddingTop(),
                bottomNav.getPaddingRight(),
                bottomNavBottomPadding + systemBars.bottom
            );

            return insets;
        });

        ViewCompat.requestApplyInsets(root);
    }

    private void syncBottomNavSelection(BottomNavigationView bottomNav, NavController navController) {
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int selectedItemId = resolveBottomNavItem(destination);
            if (selectedItemId != 0) {
                bottomNav.getMenu().findItem(selectedItemId).setChecked(true);
            }
        });
    }

    private int resolveBottomNavItem(NavDestination destination) {
        int id = destination.getId();

        if (id == R.id.homeFragment || id == R.id.courseDetailFragment
            || id == R.id.lessonStudyFragment || id == R.id.quizFragment) {
            return R.id.homeFragment;
        }

        if (id == R.id.coursesFragment || id == R.id.searchFragment
            || id == R.id.cartFragment || id == R.id.profileFragment) {
            return id;
        }

        if (id == R.id.paymentFragment) {
            return R.id.cartFragment;
        }

        return 0;
    }

    /**
     * Awards XP + updates streak once per calendar day.
     * Checks SharedPreferences to prevent duplicate awards.
     */
    private void awardDailyXpIfNeeded() {
        TokenManager tokenManager = new TokenManager(this);
        if (!tokenManager.isLoggedIn() || !tokenManager.shouldAwardDailyXp()) {
            return;
        }

        String userId = tokenManager.getUserId();
        if (userId == null) return;

        GamificationApi api = ApiClient.createAuthenticatedService(GamificationApi.class);

        // 1. Award daily login XP
        api.addXp(userId, new AddXpRequest(DAILY_LOGIN_XP)).enqueue(new Callback<GamificationStatsResponse>() {
            @Override
            public void onResponse(Call<GamificationStatsResponse> call, Response<GamificationStatsResponse> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Daily login XP awarded: +" + DAILY_LOGIN_XP);
                    tokenManager.markDailyXpAwarded();
                }
            }

            @Override
            public void onFailure(Call<GamificationStatsResponse> call, Throwable t) {
                Log.e(TAG, "Failed to award daily XP: " + t.getMessage());
            }
        });

        // 2. Update streak
        api.updateStreak(userId).enqueue(new Callback<GamificationStatsResponse>() {
            @Override
            public void onResponse(Call<GamificationStatsResponse> call, Response<GamificationStatsResponse> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Streak updated");
                }
            }

            @Override
            public void onFailure(Call<GamificationStatsResponse> call, Throwable t) {
                Log.e(TAG, "Failed to update streak: " + t.getMessage());
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                "eduflex_channel",
                "EduFlex Notifications",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Nhắc nhở học tập hằng ngày");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}