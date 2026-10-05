package com.eduflex.android.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.eduflex.android.LoginActivity;
import com.eduflex.android.api.ApiClient;
import com.eduflex.android.api.AuthApi;
import com.eduflex.android.model.LogoutRequest;
import com.eduflex.android.model.LogoutResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class SessionManager {

    private static final Object LOCK = new Object();
    private static boolean redirectingToLogin = false;

    private SessionManager() {
    }

    public static void logout(Context context) {
        Context appContext = context.getApplicationContext();
        TokenManager tokens = new TokenManager(appContext);
        String refreshToken = tokens.getRefreshToken();
        if (refreshToken == null) {
            forceLogout(appContext, "Logged out");
            return;
        }
        ApiClient.createService(AuthApi.class).logout(new LogoutRequest(refreshToken))
                .enqueue(new Callback<LogoutResponse>() {
                    @Override
                    public void onResponse(Call<LogoutResponse> call, Response<LogoutResponse> response) {
                        forceLogout(appContext, "Logged out");
                    }

                    @Override
                    public void onFailure(Call<LogoutResponse> call, Throwable throwable) {
                        // Local logout still completes. The refresh token expires server-side.
                        forceLogout(appContext, "Logged out locally");
                    }
                });
    }

    public static void forceLogout(Context context, String message) {
        Context appContext = context.getApplicationContext();

        synchronized (LOCK) {
            if (redirectingToLogin) {
                return;
            }
            redirectingToLogin = true;
        }

        TokenManager tokenManager = new TokenManager(appContext);
        tokenManager.clearToken();

        new Handler(Looper.getMainLooper()).post(() -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show();
            }

            Intent intent = new Intent(appContext, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            appContext.startActivity(intent);
        });
    }

    public static void resetLogoutState() {
        synchronized (LOCK) {
            redirectingToLogin = false;
        }
    }
}
