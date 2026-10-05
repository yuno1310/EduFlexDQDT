package com.eduflex.android.model;

public class ResetPasswordRequest {
    private final String token;
    private final String newPassword;

    public ResetPasswordRequest(String token, String newPassword) {
        this.token = token;
        this.newPassword = newPassword;
    }
}
