package com.eduflex.android.api;

import com.eduflex.android.model.AdminUserResponse;
import com.eduflex.android.model.DeleteCourseResponse;
import com.eduflex.android.model.DeleteUserResponse;
import com.eduflex.android.model.UpdateCourseRequest;
import com.eduflex.android.model.UpdateCourseResponse;
import com.eduflex.android.model.UpdateLessonRequest;
import com.eduflex.android.model.UpdateLessonResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface AdminApi {

    @GET("/api/admin/users")
    Call<AdminUserResponse> getAllUsers();

    @DELETE("/api/admin/users/{userId}")
    Call<DeleteUserResponse> deleteUser(@Path("userId") String userId);

    // ===== Courses =====
    @POST("/api/admin/courses")
    Call<UpdateCourseResponse> createCourse(@Body UpdateCourseRequest request);

    @PUT("/api/admin/courses/{courseId}")
    Call<UpdateCourseResponse> updateCourse(@Path("courseId") String courseId,
                                           @Body UpdateCourseRequest request);

    @DELETE("/api/admin/courses/{courseId}")
    Call<DeleteCourseResponse> deleteCourse(@Path("courseId") String courseId);

    // ===== Lessons =====
    @POST("/api/admin/lessons")
    Call<UpdateLessonResponse> createLesson(@Body Map<String, Object> body);

    @PUT("/api/admin/lessons/{lessonId}")
    Call<UpdateLessonResponse> updateLesson(@Path("lessonId") String lessonId,
                                           @Body UpdateLessonRequest request);

    @DELETE("/api/admin/lessons/{lessonId}")
    Call<DeleteCourseResponse> deleteLesson(@Path("lessonId") String lessonId);

    // ===== Quiz / Questions =====
    @POST("/api/admin/quizzes")
    Call<UpdateLessonResponse> createQuiz(@Body Map<String, Object> body);

    @PUT("/api/admin/quizzes/{questionId}")
    Call<UpdateLessonResponse> updateQuizRaw(@Path("questionId") long questionId,
                                            @Body Map<String, Object> body);

    @DELETE("/api/admin/questions/{questionId}")
    Call<DeleteCourseResponse> deleteQuestion(@Path("questionId") long questionId);
}
