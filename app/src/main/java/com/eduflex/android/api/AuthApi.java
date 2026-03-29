package com.eduflex.android.api;

import com.eduflex.android.model.LoginRequest;
import com.eduflex.android.model.LoginResponse;
import com.eduflex.android.model.RegisterRequest;
import com.eduflex.android.model.RegisterResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("/api/user/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("/api/user/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);
}
