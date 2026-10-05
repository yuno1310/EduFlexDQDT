package com.eduflex.android.model;

import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;
import org.junit.Test;

public class LoginResponseTest {

    @Test
    public void readsAccessTokenFromBackendResponse() {
        LoginResponse response = new Gson().fromJson(
                "{\"success\":true,\"accessToken\":\"signed-jwt\",\"refreshToken\":\"refresh-jwt\",\"role\":\"user\"}",
                LoginResponse.class);

        assertEquals("signed-jwt", response.getToken());
        assertEquals("refresh-jwt", response.getRefreshToken());
    }
}
