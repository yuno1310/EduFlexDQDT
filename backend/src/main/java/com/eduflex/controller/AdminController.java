package com.eduflex.controller;

import com.eduflex.dto.AdminDTO.DeleteCourseResponse;
import com.eduflex.dto.AdminDTO.DeleteLessonResponse;
import com.eduflex.dto.AdminDTO.DeleteQuestionResponse;
import com.eduflex.dto.AdminDTO.DeleteUserResponse;
import com.eduflex.dto.AdminDTO.GetAllUsersResponse;
import com.eduflex.dto.AdminDTO.UpdateCourseRequest;
import com.eduflex.dto.AdminDTO.UpdateCourseResponse;
import com.eduflex.dto.AdminDTO.UpdateLessonRequest;
import com.eduflex.dto.AdminDTO.UpdateLessonResponse;
import com.eduflex.dto.AdminDTO.UpdateQuizRequest;
import com.eduflex.dto.AdminDTO.UpdateQuizResponse;
import com.eduflex.dto.CreateCourseDTO.CreateCourseRequest;
import com.eduflex.dto.CreateCourseDTO.CreateCourseResponse;
import com.eduflex.dto.CreateLessonDTO.CreateLessonRequest;
import com.eduflex.dto.CreateLessonDTO.CreateLessonResponse;
import com.eduflex.dto.QuizDTO.CreateQuizRequest;
import com.eduflex.dto.QuizDTO.CreateQuizResponse;
import com.eduflex.service.CreateCourseUseCase;
import com.eduflex.service.CreateLessonUseCase;
import com.eduflex.service.CreateQuizUseCase;
import com.eduflex.service.DeleteCourseUseCase;
import com.eduflex.service.DeleteLessonUseCase;
import com.eduflex.service.DeleteQuestionUseCase;
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
    @Autowired
    private DeleteQuestionUseCase deleteQuestionUseCase;
    @Autowired
    private CreateCourseUseCase createCourseUseCase;
    @Autowired
    private CreateLessonUseCase createLessonUseCase;
    @Autowired
    private CreateQuizUseCase createQuizUseCase;

    // ===== Users =====
    @GetMapping("/users")
    public ResponseEntity<GetAllUsersResponse> getAllUsers() {
        return ResponseEntity.ok(getAllUsersUseCase.execute());
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<DeleteUserResponse> deleteUser(@PathVariable UUID userId) {
        var response = deleteUserUseCase.execute(userId);
        return response.success() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    // ===== Courses =====
    @PostMapping("/courses")
    public ResponseEntity<CreateCourseResponse> createCourse(@RequestBody CreateCourseRequest request) {
        var response = createCourseUseCase.execute(request);
        return response.success() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    @PutMapping("/courses/{courseId}")
    public ResponseEntity<UpdateCourseResponse> updateCourse(
            @PathVariable UUID courseId,
            @RequestBody UpdateCourseRequest request) {
        var response = updateCourseUseCase.execute(courseId, request);
        return response.success() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<DeleteCourseResponse> deleteCourse(@PathVariable UUID courseId) {
        var response = deleteCourseUseCase.execute(courseId);
        return response.success() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    // ===== Lessons =====
    @PostMapping("/lessons")
    public ResponseEntity<CreateLessonResponse> createLesson(@RequestBody CreateLessonRequest request) {
        var response = createLessonUseCase.execute(request);
        return response.success() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    @PutMapping("/lessons/{lessonId}")
    public ResponseEntity<UpdateLessonResponse> updateLesson(
            @PathVariable UUID lessonId,
            @RequestBody UpdateLessonRequest request) {
        var response = updateLessonUseCase.execute(lessonId, request);
        return response.success() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    @DeleteMapping("/lessons/{lessonId}")
    public ResponseEntity<DeleteLessonResponse> deleteLesson(@PathVariable UUID lessonId) {
        var response = deleteLessonUseCase.execute(lessonId);
        return response.success() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    // ===== Quiz / Questions =====
    @PostMapping("/quizzes")
    public ResponseEntity<CreateQuizResponse> createQuiz(@RequestBody CreateQuizRequest request) {
        var response = createQuizUseCase.execute(request);
        return response.success() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    @PutMapping("/quizzes/{questionId}")
    public ResponseEntity<UpdateQuizResponse> updateQuiz(
            @PathVariable Long questionId,
            @RequestBody UpdateQuizRequest request) {
        var response = updateQuizUseCase.execute(questionId, request);
        return response.success() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<DeleteQuestionResponse> deleteQuestion(@PathVariable Long questionId) {
        var response = deleteQuestionUseCase.execute(questionId);
        return response.success() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }
}
