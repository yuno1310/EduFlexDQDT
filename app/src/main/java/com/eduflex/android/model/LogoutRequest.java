package com.eduflex.android.model;

public class LogoutRequest {
    private final String refreshToken;

    public LogoutRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
