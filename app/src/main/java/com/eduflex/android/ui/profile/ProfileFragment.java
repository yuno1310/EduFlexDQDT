package com.eduflex.android.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eduflex.android.LoginActivity;
import com.eduflex.android.R;
import com.eduflex.android.adapter.BadgeAdapter;
import com.eduflex.android.api.ApiClient;
import com.eduflex.android.api.BadgeApi;
import com.eduflex.android.api.GamificationApi;
import com.eduflex.android.api.MediaApi;
import com.eduflex.android.api.UserApi;
import com.eduflex.android.auth.TokenManager;
import com.eduflex.android.model.BadgeResponse;
import com.eduflex.android.model.GamificationStatsResponse;
import com.eduflex.android.model.UpdateProfileRequest;
import com.eduflex.android.model.UpdateProfileResponse;
import com.eduflex.android.model.UserBadgeResponse;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private static final String PREF_PROFILE = "eduflex_profile";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_GOAL = "user_goal";
    private static final String KEY_PHOTO_URI = "user_photo_uri";

    private TokenManager tokenManager;
    private GamificationApi gamificationApi;
    private BadgeApi badgeApi;
    private UserApi userApi;
    private MediaApi mediaApi;
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

    // Badge views
    private RecyclerView rvBadges;
    private TextView tvBadgeCount;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK
                            && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri == null) return;
                        try {
                            requireContext().getContentResolver()
                                    .takePersistableUriPermission(imageUri,
                                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (SecurityException e) {
                            Log.w(TAG, "Could not persist URI permission: " + e.getMessage());
                        }
                        // Show locally immediately
                        ivProfileAvatar.setImageURI(imageUri);
                        // Upload to Supabase via backend
                        uploadAvatar(imageUri);
                    }
                });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tokenManager = new TokenManager(requireContext());
        gamificationApi = ApiClient.createAuthenticatedService(GamificationApi.class);
        badgeApi = ApiClient.createAuthenticatedService(BadgeApi.class);
        userApi = ApiClient.createAuthenticatedService(UserApi.class);
        mediaApi = ApiClient.createAuthenticatedService(MediaApi.class);
        profilePrefs = requireContext().getSharedPreferences(PREF_PROFILE, Context.MODE_PRIVATE);

        bindViews(view);
        loadProfile();
        fetchStats();
        fetchBadges();
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

        // Badge views
        rvBadges = view.findViewById(R.id.rv_badges);
        tvBadgeCount = view.findViewById(R.id.tv_badge_count);
        rvBadges.setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    private void loadProfile() {
        // Seed from TokenManager on first load so login data is reflected
        if (profilePrefs.getString(KEY_NAME, null) == null && tokenManager.getFullName() != null) {
            profilePrefs.edit().putString(KEY_NAME, tokenManager.getFullName()).apply();
        }
        if (profilePrefs.getString(KEY_EMAIL, null) == null && tokenManager.getEmail() != null) {
            profilePrefs.edit().putString(KEY_EMAIL, tokenManager.getEmail()).apply();
        }
        if (profilePrefs.getString(KEY_PHOTO_URI, null) == null && tokenManager.getAvatarUrl() != null) {
            profilePrefs.edit().putString(KEY_PHOTO_URI, tokenManager.getAvatarUrl()).apply();
        }

        String name = profilePrefs.getString(KEY_NAME, "Student");
        String email = profilePrefs.getString(KEY_EMAIL, "");
        String goal = profilePrefs.getString(KEY_GOAL, "");
        String photoUri = profilePrefs.getString(KEY_PHOTO_URI, null);

        tvProfileName.setText(name);
        tvProfileEmail.setText(email.isEmpty() ? "EduFlex Learner" : email);

        if (!goal.isEmpty()) {
            tvGoalDisplay.setText(goal);
        } else {
            tvGoalDisplay.setText(R.string.set_your_goal);
        }

        if (photoUri != null) {
            if (photoUri.startsWith("http")) {
                com.bumptech.glide.Glide.with(this).load(photoUri).circleCrop().into(ivProfileAvatar);
            } else {
                try {
                    ivProfileAvatar.setImageURI(Uri.parse(photoUri));
                } catch (Exception e) {
                    Log.e(TAG, "Failed to load saved photo: " + e.getMessage());
                }
            }
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

    // ── Badge Display ──

    private void fetchBadges() {
        String userId = tokenManager.getUserId();
        if (userId == null) return;

        badgeApi.getAllBadges().enqueue(new Callback<List<BadgeResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<BadgeResponse>> call,
                                   @NonNull Response<List<BadgeResponse>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    List<BadgeResponse> allBadges = response.body();
                    fetchUserBadgesAndRender(userId, allBadges);
                } else {
                    Log.e(TAG, "Badge load failed: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<BadgeResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "Badge network error: " + t.getMessage());
            }
        });
    }

    private void fetchUserBadgesAndRender(String userId, List<BadgeResponse> allBadges) {
        badgeApi.getUserBadges(userId).enqueue(new Callback<List<UserBadgeResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<UserBadgeResponse>> call,
                                   @NonNull Response<List<UserBadgeResponse>> response) {
                if (!isAdded()) return;

                Set<Long> earnedIds = new HashSet<>();
                if (response.isSuccessful() && response.body() != null) {
                    for (UserBadgeResponse ub : response.body()) {
                        earnedIds.add(ub.getBadgeId());
                    }
                }

                allBadges.sort((b1, b2) -> {
                    boolean e1 = earnedIds.contains(b1.getId());
                    boolean e2 = earnedIds.contains(b2.getId());
                    if (e1 == e2) return b1.getName().compareToIgnoreCase(b2.getName());
                    return e1 ? -1 : 1;
                });

                tvBadgeCount.setText(String.valueOf(earnedIds.size()));
                rvBadges.setAdapter(new BadgeAdapter(allBadges, earnedIds));
            }

            @Override
            public void onFailure(@NonNull Call<List<UserBadgeResponse>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "User badges network error: " + t.getMessage());
                tvBadgeCount.setText("0");
                rvBadges.setAdapter(new BadgeAdapter(allBadges, new HashSet<>()));
            }
        });
    }

    // ── Listeners ──

    private void setupListeners() {
        btnEditProfile.setOnClickListener(v -> toggleEditMode(true));
        btnCancelEdit.setOnClickListener(v -> toggleEditMode(false));

        btnSaveName.setOnClickListener(v -> {
            String newName = etEditName.getText().toString().trim();
            if (newName.isEmpty()) {
                toggleEditMode(false);
                return;
            }
            String userId = tokenManager.getUserId();
            if (userId == null) {
                Toast.makeText(requireContext(), "Session expired, please log in again", Toast.LENGTH_SHORT).show();
                return;
            }
            userApi.updateProfile(userId, new UpdateProfileRequest(newName))
                .enqueue(new Callback<UpdateProfileResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<UpdateProfileResponse> call,
                                           @NonNull Response<UpdateProfileResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            profilePrefs.edit().putString(KEY_NAME, newName).apply();
                            tvProfileName.setText(newName);
                            Toast.makeText(requireContext(), "Name updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Failed to update name", Toast.LENGTH_SHORT).show();
                        }
                        toggleEditMode(false);
                    }
                    @Override
                    public void onFailure(@NonNull Call<UpdateProfileResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
                        toggleEditMode(false);
                    }
                });
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

        // Goal custom text (tap goal display to edit)
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

        // Camera badge — open gallery to pick a photo
        ImageView ivCameraBadge = requireView().findViewById(R.id.iv_camera_badge);
        ivCameraBadge.setOnClickListener(v -> openImagePicker());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        imagePickerLauncher.launch(intent);
    }

    private void uploadAvatar(Uri imageUri) {
        String userId = tokenManager.getUserId();
        if (userId == null) return;

        try {
            android.content.ContentResolver resolver = requireContext().getContentResolver();
            String mimeType = resolver.getType(imageUri);
            if (mimeType == null) mimeType = "image/jpeg";

            java.io.InputStream inputStream = resolver.openInputStream(imageUri);
            if (inputStream == null) return;
            byte[] bytes = inputStream.readAllBytes();
            inputStream.close();

            String ext = mimeType.contains("png") ? ".png" : ".jpg";
            String filename = "avatar_" + userId + ext;

            RequestBody requestBody = RequestBody.create(bytes, MediaType.parse(mimeType));
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", filename, requestBody);

            mediaApi.uploadAvatar(userId, part).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(@NonNull Call<Map<String, Object>> call,
                                       @NonNull Response<Map<String, Object>> response) {
                    if (!isAdded()) return;
                    if (response.isSuccessful() && response.body() != null) {
                        Object urlObj = response.body().get("url");
                        if (urlObj instanceof String) {
                            String url = (String) urlObj;
                            profilePrefs.edit().putString(KEY_PHOTO_URI, url).apply();
                            com.bumptech.glide.Glide.with(ProfileFragment.this).load(url).circleCrop().into(ivProfileAvatar);
                        }
                        Toast.makeText(requireContext(), "Avatar updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to upload avatar", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                    if (!isAdded()) return;
                    Log.e(TAG, "Avatar upload error: " + t.getMessage());
                    Toast.makeText(requireContext(), "Network error uploading avatar", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Failed to read image: " + e.getMessage());
            Toast.makeText(requireContext(), "Could not read image", Toast.LENGTH_SHORT).show();
        }
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
