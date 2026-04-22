package com.eduflex.android.api;

import com.eduflex.android.model.ForgotPasswordRequest;
import com.eduflex.android.model.ForgotPasswordResponse;
import com.eduflex.android.model.UpdateProfileRequest;
import com.eduflex.android.model.UpdateProfileResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserApi {

    @PUT("/api/user/update-profile/{userId}")
    Call<UpdateProfileResponse> updateProfile(
        @Path("userId") String userId,
        @Body UpdateProfileRequest request
    );

    @POST("/api/user/forgot-password")
    Call<ForgotPasswordResponse> forgotPassword(@Body ForgotPasswordRequest request);
}
