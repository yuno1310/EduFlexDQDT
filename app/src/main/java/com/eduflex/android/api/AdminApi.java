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
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface AdminApi {

    @GET("/api/admin/users")
    Call<AdminUserResponse> getAllUsers();

    @DELETE("/api/admin/users/{userId}")
    Call<DeleteUserResponse> deleteUser(@Path("userId") String userId);

    @DELETE("/api/admin/courses/{courseId}")
    Call<DeleteCourseResponse> deleteCourse(@Path("courseId") String courseId);

    @DELETE("/api/admin/lessons/{lessonId}")
    Call<DeleteCourseResponse> deleteLesson(@Path("lessonId") String lessonId);

    @PUT("/api/admin/courses/{courseId}")
    Call<UpdateCourseResponse> updateCourse(@Path("courseId") String courseId,
                                           @Body UpdateCourseRequest request);

    @PUT("/api/admin/lessons/{lessonId}")
    Call<UpdateLessonResponse> updateLesson(@Path("lessonId") String lessonId,
                                           @Body UpdateLessonRequest request);

    @PUT("/api/admin/quizzes/{questionId}")
    Call<UpdateLessonResponse> updateQuizRaw(@Path("questionId") long questionId,
                                            @Body Map<String, Object> body);
}
