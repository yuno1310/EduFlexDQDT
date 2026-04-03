package com.eduflex.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.eduflex.android.api.ApiClient;
import com.eduflex.android.api.AuthApi;
import com.eduflex.android.model.RegisterRequest;
import com.eduflex.android.model.RegisterResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final String PREF_PROFILE = "eduflex_profile";

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvGoToLogin;
    private ProgressBar progressBar;
    private TextView chipCareer, chipSkill, chipHobby, chipAcademic;
    private String selectedGoal = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_register);
        applyEdgeToEdgeInsets();

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvGoToLogin = findViewById(R.id.tv_go_to_login);
        progressBar = findViewById(R.id.progress_bar);

        chipCareer = findViewById(R.id.chip_reg_career);
        chipSkill = findViewById(R.id.chip_reg_skill);
        chipHobby = findViewById(R.id.chip_reg_hobby);
        chipAcademic = findViewById(R.id.chip_reg_academic);

        setupGoalChips();

        btnRegister.setOnClickListener(v -> attemptRegister());
        tvGoToLogin.setOnClickListener(v -> finish());

        ImageView ivAddPhoto = findViewById(R.id.iv_add_photo);
        ivAddPhoto.setOnClickListener(v ->
                Toast.makeText(this, "Photo upload coming soon!", Toast.LENGTH_SHORT).show());
    }

    private void setupGoalChips() {
        View.OnClickListener chipListener = v -> {
            // Reset all chip backgrounds
            chipCareer.setBackgroundResource(R.drawable.bg_goal_chip);
            chipSkill.setBackgroundResource(R.drawable.bg_goal_chip);
            chipHobby.setBackgroundResource(R.drawable.bg_goal_chip);
            chipAcademic.setBackgroundResource(R.drawable.bg_goal_chip);

            // Highlight selected
            v.setBackgroundResource(R.drawable.bg_chip_tonal_rounded);
            selectedGoal = ((TextView) v).getText().toString();
        };

        chipCareer.setOnClickListener(chipListener);
        chipSkill.setOnClickListener(chipListener);
        chipHobby.setOnClickListener(chipListener);
        chipAcademic.setOnClickListener(chipListener);
    }

    private void attemptRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        AuthApi authApi = ApiClient.createService(AuthApi.class);
        authApi.register(new RegisterRequest(email, password, name)).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Save profile info locally for the profile screen
                    saveProfileData(name, email);
                    Toast.makeText(RegisterActivity.this, "Registration successful! Please log in.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Registration failed";
                    Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfileData(String name, String email) {
        SharedPreferences prefs = getSharedPreferences(PREF_PROFILE, MODE_PRIVATE);
        prefs.edit()
                .putString("user_name", name)
                .putString("user_email", email)
                .putString("user_goal", selectedGoal)
                .apply();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!loading);
    }

    private void applyEdgeToEdgeInsets() {
        View root = findViewById(R.id.root_register);
        final int originalTop = root.getPaddingTop();
        final int originalBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            root.setPadding(
                root.getPaddingLeft(),
                originalTop + systemBars.top,
                root.getPaddingRight(),
                originalBottom + systemBars.bottom
            );
            return insets;
        });

        ViewCompat.requestApplyInsets(root);
    }
}
