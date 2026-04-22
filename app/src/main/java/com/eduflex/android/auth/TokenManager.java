package com.eduflex.android.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

public class TokenManager {

    private static final String TAG = "TokenManager";
    private static final String PREF_NAME = "eduflex_auth";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_ROLE = "user_role";
    private static final String KEY_FULL_NAME = "user_full_name";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_AVATAR_URL = "user_avatar_url";

    private final SharedPreferences prefs;

    public TokenManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void saveRole(String role) {
        prefs.edit().putString(KEY_ROLE, role != null ? role : "user").apply();
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "user");
    }

    public boolean isAdmin() {
        return "admin".equals(getRole());
    }

    public void saveFullName(String fullName) {
        prefs.edit().putString(KEY_FULL_NAME, fullName).apply();
    }

    public String getFullName() {
        return prefs.getString(KEY_FULL_NAME, null);
    }

    public void saveEmail(String email) {
        prefs.edit().putString(KEY_EMAIL, email).apply();
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public void saveAvatarUrl(String url) {
        prefs.edit().putString(KEY_AVATAR_URL, url).apply();
    }

    public String getAvatarUrl() {
        return prefs.getString(KEY_AVATAR_URL, null);
    }

    public void clearToken() {
        prefs.edit().remove(KEY_TOKEN).remove(KEY_ROLE).remove(KEY_FULL_NAME).remove(KEY_EMAIL).remove(KEY_AVATAR_URL).apply();
    }

    /**
     * Extracts the user ID (subject) from the JWT token payload.
     * Returns null if the token is missing or cannot be decoded.
     */
    public String getUserId() {
        String token = getToken();
        if (token == null) return null;
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;
            String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP));
            JSONObject json = new JSONObject(payload);
            return json.optString("sub", null);
        } catch (Exception e) {
            Log.e(TAG, "Failed to decode JWT: " + e.getMessage());
            return null;
        }
    }

    // ── Daily XP tracking ──

    private static final String KEY_LAST_DAILY_XP_DATE = "last_daily_xp_date";

    /**
     * Returns true if daily XP has NOT been awarded yet today.
     */
    public boolean shouldAwardDailyXp() {
        String lastDate = prefs.getString(KEY_LAST_DAILY_XP_DATE, "");
        String today = java.time.LocalDate.now().toString(); // "2026-03-29"
        return !today.equals(lastDate);
    }

    /**
     * Mark that daily XP has been awarded for today.
     */
    public void markDailyXpAwarded() {
        String today = java.time.LocalDate.now().toString();
        prefs.edit().putString(KEY_LAST_DAILY_XP_DATE, today).apply();
    }
}
