package com.eduflex.android.api;

import com.eduflex.android.model.AddXpRequest;
import com.eduflex.android.model.GamificationStatsResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface GamificationApi {

    @GET("/api/users/{userId}/stats")
    Call<GamificationStatsResponse> getStats(@Path("userId") String userId);

    @POST("/api/users/{userId}/xp")
    Call<GamificationStatsResponse> addXp(
            @Path("userId") String userId,
            @Body AddXpRequest body);

    @POST("/api/users/{userId}/streak")
    Call<GamificationStatsResponse> updateStreak(@Path("userId") String userId);
}
