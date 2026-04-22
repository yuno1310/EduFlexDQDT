package com.eduflex.controller;

import com.eduflex.dto.AdminDTO.DeleteCourseResponse;
import com.eduflex.dto.AdminDTO.DeleteLessonResponse;
import com.eduflex.dto.AdminDTO.DeleteUserResponse;
import com.eduflex.dto.AdminDTO.GetAllUsersResponse;
import com.eduflex.dto.AdminDTO.UpdateCourseRequest;
import com.eduflex.dto.AdminDTO.UpdateCourseResponse;
import com.eduflex.dto.AdminDTO.UpdateLessonRequest;
import com.eduflex.dto.AdminDTO.UpdateLessonResponse;
import com.eduflex.dto.AdminDTO.UpdateQuizRequest;
import com.eduflex.dto.AdminDTO.UpdateQuizResponse;
import com.eduflex.service.DeleteCourseUseCase;
import com.eduflex.service.DeleteLessonUseCase;
import com.eduflex.service.DeleteUserUseCase;
import com.eduflex.service.GetAllUsersUseCase;
import com.eduflex.service.UpdateCourseUseCase;
import com.eduflex.service.UpdateLessonUseCase;
import com.eduflex.service.UpdateQuizUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private GetAllUsersUseCase getAllUsersUseCase;

    @Autowired
    private DeleteUserUseCase deleteUserUseCase;

    @Autowired
    private DeleteCourseUseCase deleteCourseUseCase;

    @Autowired
    private UpdateCourseUseCase updateCourseUseCase;

    @Autowired
    private UpdateLessonUseCase updateLessonUseCase;

    @Autowired
    private DeleteLessonUseCase deleteLessonUseCase;

    @Autowired
    private UpdateQuizUseCase updateQuizUseCase;

    @GetMapping("/users")
    public ResponseEntity<GetAllUsersResponse> getAllUsers() {
        return ResponseEntity.ok(getAllUsersUseCase.execute());
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<DeleteUserResponse> deleteUser(@PathVariable UUID userId) {
        var response = deleteUserUseCase.execute(userId);
        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<DeleteCourseResponse> deleteCourse(@PathVariable UUID courseId) {
        var response = deleteCourseUseCase.execute(courseId);
        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/lessons/{lessonId}")
    public ResponseEntity<DeleteLessonResponse> deleteLesson(@PathVariable UUID lessonId) {
        var response = deleteLessonUseCase.execute(lessonId);
        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/courses/{courseId}")
    public ResponseEntity<UpdateCourseResponse> updateCourse(
            @PathVariable UUID courseId,
            @RequestBody UpdateCourseRequest request) {
        var response = updateCourseUseCase.execute(courseId, request);
        return response.success() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    @PutMapping("/lessons/{lessonId}")
    public ResponseEntity<UpdateLessonResponse> updateLesson(
            @PathVariable UUID lessonId,
            @RequestBody UpdateLessonRequest request) {
        var response = updateLessonUseCase.execute(lessonId, request);
        return response.success() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    @PutMapping("/quizzes/{questionId}")
    public ResponseEntity<UpdateQuizResponse> updateQuiz(
            @PathVariable Long questionId,
            @RequestBody UpdateQuizRequest request) {
        var response = updateQuizUseCase.execute(questionId, request);
        return response.success() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }
}
