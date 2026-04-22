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
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.eduflex.android.api.ApiClient;
import com.eduflex.android.api.GamificationApi;
import com.eduflex.android.auth.TokenManager;
import com.eduflex.android.model.GamificationStatsResponse;
import com.eduflex.android.service.StudyReminderWorker;
import com.google.firebase.messaging.FirebaseMessaging;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String REMINDER_WORK_TAG = "daily_study_reminder";

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

        // Create notification channels & request permission (Android 13+)
        createNotificationChannel();
        requestNotificationPermission();

        // Daily check-in: awards +10 XP once per day (server prevents duplicates)
        performDailyCheckin();

        // Schedule daily study reminder notification
        scheduleDailyStudyReminder();
    }

    /**
     * Calls server-side daily-checkin endpoint.
     * Server uses last_login_xp_date to ensure XP is only awarded once per day.
     * Safe to call multiple times — no duplicates.
     */
    private void performDailyCheckin() {
        TokenManager tokenManager = new TokenManager(this);
        if (!tokenManager.isLoggedIn())
            return;

        String userId = tokenManager.getUserId();
        if (userId == null)
            return;

        GamificationApi api = ApiClient.createAuthenticatedService(GamificationApi.class);
        api.dailyCheckin(userId).enqueue(new Callback<GamificationStatsResponse>() {
            @Override
            public void onResponse(Call<GamificationStatsResponse> call,
                    Response<GamificationStatsResponse> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Daily check-in completed");
                } else {
                    Log.e(TAG, "Daily check-in failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GamificationStatsResponse> call, Throwable t) {
                Log.e(TAG, "Daily check-in error: " + t.getMessage());
            }
        });
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
                    navHost.getPaddingBottom());

            bottomNav.setPadding(
                    bottomNav.getPaddingLeft(),
                    bottomNav.getPaddingTop(),
                    bottomNav.getPaddingRight(),
                    bottomNavBottomPadding + systemBars.bottom);

            return insets;
        });

        ViewCompat.requestApplyInsets(root);
    }

    private void syncBottomNavSelection(BottomNavigationView bottomNav, NavController navController) {
        NavigationUI.setupWithNavController(bottomNav, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int selectedItemId = resolveBottomNavItem(destination, arguments);
            if (selectedItemId != 0) {
                bottomNav.getMenu().findItem(selectedItemId).setChecked(true);
            }
        });
    }

    private int resolveBottomNavItem(NavDestination destination, android.os.Bundle arguments) {
        int id = destination.getId();

        if (id == R.id.courseDetailFragment
                || id == R.id.lessonStudyFragment || id == R.id.quizFragment
                || id == R.id.leaderboardFragment || id == R.id.courseReviewFragment
                || id == R.id.certificateFragment || id == R.id.quizResultFragment
                || id == R.id.fillBlankQuizMockFragment || id == R.id.aiCourseSummaryFragment) {
            int sourceTab = arguments != null ? arguments.getInt("sourceTab", 0) : 0;
            return sourceTab != 0 ? sourceTab : R.id.homeFragment;
        }

        if (id == R.id.homeFragment || id == R.id.coursesFragment
                || id == R.id.searchFragment || id == R.id.cartFragment
                || id == R.id.profileFragment) {
            return id;
        }

        if (id == R.id.paymentFragment) {
            return R.id.cartFragment;
        }

        return 0;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);

            // FCM push channel
            NotificationChannel fcmChannel = new NotificationChannel(
                    "eduflex_channel",
                    "EduFlex Notifications",
                    NotificationManager.IMPORTANCE_HIGH);
            fcmChannel.setDescription("Thông báo từ EduFlex");
            manager.createNotificationChannel(fcmChannel);

            // Study reminder channel
            NotificationChannel reminderChannel = new NotificationChannel(
                    "study_reminder_channel",
                    "Study Reminders",
                    NotificationManager.IMPORTANCE_HIGH);
            reminderChannel.setDescription("Nhắc nhở học tập hằng ngày");
            manager.createNotificationChannel(reminderChannel);
        }
    }

    /**
     * Request notification permission on Android 13+ (API 33+).
     * Without this, notifications are silently blocked.
     */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.POST_NOTIFICATIONS }, 1001);
            }
        }
    }

    /**
     * Schedule a daily study reminder notification around 8:00 PM.
     * Uses KEEP policy so it won't reset the timer if already scheduled.
     */
    private void scheduleDailyStudyReminder() {
        // Calculate initial delay to 8:00 PM today (or tomorrow if past 8 PM)
        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, 20); // 8 PM
        target.set(Calendar.MINUTE, 0);
        target.set(Calendar.SECOND, 0);

        long now = System.currentTimeMillis();
        long delay = target.getTimeInMillis() - now;
        if (delay < 0) {
            // Already past 8 PM today → schedule for tomorrow
            delay += TimeUnit.DAYS.toMillis(1);
        }

        PeriodicWorkRequest reminderRequest = new PeriodicWorkRequest.Builder(
                StudyReminderWorker.class,
                1, TimeUnit.DAYS // repeat every 24h
        )
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag(REMINDER_WORK_TAG)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                REMINDER_WORK_TAG,
                ExistingPeriodicWorkPolicy.KEEP, // Replace old schedule with new one
                reminderRequest);

        Log.d(TAG, "Daily study reminder scheduled (initial delay: "
                + TimeUnit.MILLISECONDS.toHours(delay) + "h)");
    }
}
