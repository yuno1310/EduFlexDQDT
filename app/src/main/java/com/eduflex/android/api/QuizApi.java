package com.eduflex.android.api;

import com.eduflex.android.model.QuizGetResponse;
import com.eduflex.android.model.SubmitQuizRequest;
import com.eduflex.android.model.SubmitQuizResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface QuizApi {

    @GET("api/quiz/{lessonId}")
    Call<QuizGetResponse> getQuiz(@Path("lessonId") String lessonId);

    @POST("api/quiz/submit")
    Call<SubmitQuizResponse> submitQuiz(@Body SubmitQuizRequest request);
}
