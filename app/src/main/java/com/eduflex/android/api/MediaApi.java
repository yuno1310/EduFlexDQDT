package com.eduflex.android.api;

import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface MediaApi {

    @Multipart
    @POST("/api/media/users/{userId}/avatar")
    Call<Map<String, Object>> uploadAvatar(
        @Path("userId") String userId,
        @Part MultipartBody.Part file
    );
}
