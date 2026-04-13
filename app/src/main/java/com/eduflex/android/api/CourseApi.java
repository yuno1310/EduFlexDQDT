package com.eduflex.android.api;

import com.eduflex.android.model.CategoryListResponse;
import com.eduflex.android.model.CourseListResponse;
import com.eduflex.android.model.EnrolledCoursesResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CourseApi {

    @GET("api/course")
    Call<CourseListResponse> getCourses();

    @GET("api/categories")
    Call<CategoryListResponse> getCategories();

    @GET("api/enrollment/{userId}")
    Call<EnrolledCoursesResponse> getEnrolledCourses(@Path("userId") String userId);
}
