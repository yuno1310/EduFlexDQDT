package com.eduflex.android.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.eduflex.android.LoginActivity;
import com.eduflex.android.R;
import com.eduflex.android.api.ApiClient;
import com.eduflex.android.api.GamificationApi;
import com.eduflex.android.auth.TokenManager;
import com.eduflex.android.model.GamificationStatsResponse;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private static final String PREF_PROFILE = "eduflex_profile";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_GOAL = "user_goal";

    private TokenManager tokenManager;
    private GamificationApi gamificationApi;
    private SharedPreferences profilePrefs;

    // Views
    private ImageView ivProfileAvatar;
    private TextView tvProfileName, tvProfileEmail;
    private TextView tvStatXp, tvStatLevel, tvStatStreak;
    private TextView tvGoalDisplay;
    private TextInputLayout tilGoal;
    private EditText etGoal;
    private CardView cardEditName;
    private EditText etEditName;
    private Button btnEditProfile, btnSaveName, btnCancelEdit, btnLogout;
    private View llGoalChips;
    private TextView chipCareer, chipSkill, chipHobby, chipAcademic;

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tokenManager = new TokenManager(requireContext());
        gamificationApi = ApiClient.createAuthenticatedService(GamificationApi.class);
        profilePrefs = requireContext().getSharedPreferences(PREF_PROFILE, Context.MODE_PRIVATE);

        bindViews(view);
        loadProfile();
        fetchStats();
        setupListeners();
    }

    private void bindViews(View view) {
        ivProfileAvatar = view.findViewById(R.id.iv_profile_avatar);
        tvProfileName = view.findViewById(R.id.tv_profile_name);
        tvProfileEmail = view.findViewById(R.id.tv_profile_email);
        tvStatXp = view.findViewById(R.id.tv_stat_xp);
        tvStatLevel = view.findViewById(R.id.tv_stat_level);
        tvStatStreak = view.findViewById(R.id.tv_stat_streak);
        tvGoalDisplay = view.findViewById(R.id.tv_goal_display);
        tilGoal = view.findViewById(R.id.til_goal);
        etGoal = view.findViewById(R.id.et_goal);
        cardEditName = view.findViewById(R.id.card_edit_name);
        etEditName = view.findViewById(R.id.et_edit_name);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnSaveName = view.findViewById(R.id.btn_save_name);
        btnCancelEdit = view.findViewById(R.id.btn_cancel_edit);
        btnLogout = view.findViewById(R.id.btn_logout);
        llGoalChips = view.findViewById(R.id.ll_goal_chips);
        chipCareer = view.findViewById(R.id.chip_career);
        chipSkill = view.findViewById(R.id.chip_skill);
        chipHobby = view.findViewById(R.id.chip_hobby);
        chipAcademic = view.findViewById(R.id.chip_academic);
    }

    private void loadProfile() {
        String name = profilePrefs.getString(KEY_NAME, "Student");
        String email = profilePrefs.getString(KEY_EMAIL, "");
        String goal = profilePrefs.getString(KEY_GOAL, "");

        tvProfileName.setText(name);
        tvProfileEmail.setText(email.isEmpty() ? "EduFlex Learner" : email);

        if (!goal.isEmpty()) {
            tvGoalDisplay.setText(goal);
        } else {
            tvGoalDisplay.setText(R.string.set_your_goal);
        }
    }

    private void fetchStats() {
        String userId = tokenManager.getUserId();
        if (userId == null) {
            Log.e(TAG, "No user ID from token");
            return;
        }

        gamificationApi.getStats(userId).enqueue(new Callback<GamificationStatsResponse>() {
            @Override
            public void onResponse(@NonNull Call<GamificationStatsResponse> call,
                                   @NonNull Response<GamificationStatsResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    GamificationStatsResponse stats = response.body();
                    tvStatXp.setText(String.valueOf(stats.getXp()));
                    tvStatLevel.setText(String.valueOf(stats.getLevel()));
                    tvStatStreak.setText(String.valueOf(stats.getStreakDays()));
                } else {
                    Log.e(TAG, "Stats load failed: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<GamificationStatsResponse> call,
                                  @NonNull Throwable t) {
                Log.e(TAG, "Stats network error: " + t.getMessage());
            }
        });
    }

    private void setupListeners() {
        btnEditProfile.setOnClickListener(v -> toggleEditMode(true));
        btnCancelEdit.setOnClickListener(v -> toggleEditMode(false));

        btnSaveName.setOnClickListener(v -> {
            String newName = etEditName.getText().toString().trim();
            if (!newName.isEmpty()) {
                profilePrefs.edit().putString(KEY_NAME, newName).apply();
                tvProfileName.setText(newName);
                Toast.makeText(requireContext(), "Name updated", Toast.LENGTH_SHORT).show();
            }
            toggleEditMode(false);
        });

        // Goal chips
        View.OnClickListener goalChipListener = v -> {
            String goalText = ((TextView) v).getText().toString();
            setGoal(goalText);
        };
        chipCareer.setOnClickListener(goalChipListener);
        chipSkill.setOnClickListener(goalChipListener);
        chipHobby.setOnClickListener(goalChipListener);
        chipAcademic.setOnClickListener(goalChipListener);

        // Goal custom text (long-press goal display to edit)
        tvGoalDisplay.setOnClickListener(v -> {
            tilGoal.setVisibility(View.VISIBLE);
            llGoalChips.setVisibility(View.VISIBLE);
            etGoal.setText(profilePrefs.getString(KEY_GOAL, ""));
            etGoal.requestFocus();
        });

        // Save custom goal when focus is lost from the goal edit text
        etGoal.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String customGoal = etGoal.getText().toString().trim();
                if (!customGoal.isEmpty()) {
                    setGoal(customGoal);
                }
                tilGoal.setVisibility(View.GONE);
            }
        });

        // Logout
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());

        // Camera badge (placeholder action)
        ImageView ivCameraBadge = requireView().findViewById(R.id.iv_camera_badge);
        ivCameraBadge.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Photo upload coming soon!", Toast.LENGTH_SHORT).show());
    }

    private void toggleEditMode(boolean editing) {
        cardEditName.setVisibility(editing ? View.VISIBLE : View.GONE);
        if (editing) {
            etEditName.setText(tvProfileName.getText());
            etEditName.requestFocus();
        }
    }

    private void setGoal(String goal) {
        profilePrefs.edit().putString(KEY_GOAL, goal).apply();
        tvGoalDisplay.setText(goal);
        tilGoal.setVisibility(View.GONE);
        Toast.makeText(requireContext(), "Goal updated!", Toast.LENGTH_SHORT).show();
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_confirm)
                .setPositiveButton(R.string.logout, (dialog, which) -> {
                    tokenManager.clearToken();
                    profilePrefs.edit().clear().apply();
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}