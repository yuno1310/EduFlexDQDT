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

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public void clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply();
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
}
