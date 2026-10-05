package com.eduflex.android.api;

import com.eduflex.android.model.LoginRequest;
import com.eduflex.android.model.LoginResponse;
import com.eduflex.android.model.RegisterRequest;
import com.eduflex.android.model.RegisterResponse;
import com.eduflex.android.model.RefreshTokenRequest;
import com.eduflex.android.model.RefreshTokenResponse;
import com.eduflex.android.model.LogoutRequest;
import com.eduflex.android.model.LogoutResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("/api/user/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("/api/user/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("/api/auth/refresh")
    Call<RefreshTokenResponse> refresh(@Body RefreshTokenRequest request);

    @POST("/api/auth/logout")
    Call<LogoutResponse> logout(@Body LogoutRequest request);
}
