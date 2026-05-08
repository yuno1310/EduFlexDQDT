package com.eduflex.android.api;

import com.eduflex.android.model.DailyQuestResponse;
import com.eduflex.android.model.QuestProgressRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface DailyQuestApi {

    @GET("/api/users/{userId}/daily-quests")
    Call<List<DailyQuestResponse>> getDailyQuests(@Path("userId") String userId);

    @POST("/api/users/{userId}/daily-quests/progress")
    Call<Void> reportProgress(
            @Path("userId") String userId,
            @Body QuestProgressRequest body);
}
