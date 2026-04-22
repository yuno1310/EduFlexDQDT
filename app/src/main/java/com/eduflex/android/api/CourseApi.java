package com.eduflex.android.api;

import com.eduflex.android.model.CategoryListResponse;
import com.eduflex.android.model.CourseListResponse;
import com.eduflex.android.model.CourseSearchResult;
import com.eduflex.android.model.EnrollRequest;
import com.eduflex.android.model.EnrollResponse;
import com.eduflex.android.model.EnrolledCoursesResponse;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CourseApi {

    @GET("api/course")
    Call<CourseListResponse> getCourses();

    @GET("api/categories")
    Call<CategoryListResponse> getCategories();

    @GET("api/enrollment/{userId}")
    Call<EnrolledCoursesResponse> getEnrolledCourses(@Path("userId") String userId);

    @GET("api/course/search")
    Call<List<CourseSearchResult>> searchCourses(@Query("keyword") String keyword);

    @POST("api/enrollment/{courseId}/register")
    Call<EnrollResponse> enrollCourse(@Path("courseId") String courseId, @Body EnrollRequest request);
}
