package com.eduflex.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.eduflex.android.api.ApiClient;
import com.eduflex.android.auth.TokenManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_admin);
        applyEdgeToEdgeInsets();

        // Init API client
        ApiClient.init(this);

        // Setup bottom nav
        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.admin_nav_host);
        NavController navController = navHostFragment.getNavController();

        BottomNavigationView bottomNav = findViewById(R.id.admin_bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);
    }

    /**
     * Called from AdminProfileFragment to logout the admin.
     */
    public void logoutAdmin() {
        TokenManager tokenManager = new TokenManager(this);
        tokenManager.clearToken();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Apply edge-to-edge insets — identical to MainActivity.
     * Top inset goes to navHost, bottom inset goes to bottomNav.
     */
    private void applyEdgeToEdgeInsets() {
        View root = findViewById(R.id.root_admin);
        View navHost = findViewById(R.id.admin_nav_host);
        View bottomNav = findViewById(R.id.admin_bottom_nav);

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
}
