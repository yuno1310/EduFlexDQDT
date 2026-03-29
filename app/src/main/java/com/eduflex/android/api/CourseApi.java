package com.eduflex.android.api;

import com.eduflex.android.model.CategoryListResponse;
import com.eduflex.android.model.CourseListResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CourseApi {

    @GET("api/course")
    Call<CourseListResponse> getCourses();

    @GET("api/categories")
    Call<CategoryListResponse> getCategories();
}
