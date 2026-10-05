package com.eduflex.android.model;

public class RefreshTokenResponse {
    private boolean success;
    private String accessToken;

    public boolean isSuccess() { return success; }
    public String getAccessToken() { return accessToken; }
}
