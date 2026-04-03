package com.eduflex.android.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.eduflex.android.LoginActivity;

public final class SessionManager {

    private static final Object LOCK = new Object();
    private static boolean redirectingToLogin = false;

    private SessionManager() {
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