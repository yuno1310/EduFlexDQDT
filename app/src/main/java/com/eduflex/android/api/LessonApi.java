package com.eduflex.android.api;

import com.eduflex.android.model.LessonListResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LessonApi {

    @GET("api/lesson")
    Call<LessonListResponse> getLessons(@Query("courseID") String courseID);
}
