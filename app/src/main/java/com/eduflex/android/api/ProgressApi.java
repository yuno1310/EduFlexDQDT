package com.eduflex.android.api;

import com.eduflex.android.model.SaveProgressResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ProgressApi {

    @FormUrlEncoded
    @POST("api/progress/lesson")
    Call<SaveProgressResponse> saveLessonProgress(
            @Field("lessonId") String lessonId,
            @Field("userId") String userId);
}
