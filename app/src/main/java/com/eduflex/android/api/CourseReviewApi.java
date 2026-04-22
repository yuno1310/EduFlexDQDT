package com.eduflex.android.api;

import com.eduflex.android.model.CourseReviewListResponse;
import com.eduflex.android.model.CourseReviewRequest;
import com.eduflex.android.model.CourseReviewSubmitResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CourseReviewApi {

    @GET("api/course/{courseId}/reviews")
    Call<CourseReviewListResponse> getCourseReviews(@Path("courseId") String courseId);

    @POST("api/course/{courseId}/reviews")
    Call<CourseReviewSubmitResponse> submitReview(@Path("courseId") String courseId,
                                                  @Body CourseReviewRequest request);
}
