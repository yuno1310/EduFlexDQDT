package com.eduflex.controller;

import com.eduflex.dto.CourseProgressDTO.GetCourseProgressResponse;
import com.eduflex.dto.ProgressDTO.SaveLessonRequest;
import com.eduflex.dto.ProgressDTO.SaveLessonResponse;
import com.eduflex.service.GetCourseProgressUseCase;
import com.eduflex.service.SaveLessonProgressUseCase;

import jakarta.validation.Valid;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

  @Autowired
  private SaveLessonProgressUseCase saveLessonProgressUseCase;

  @Autowired
  private GetCourseProgressUseCase getCourseProgressUseCase;

  @PostMapping("/lesson")
  public ResponseEntity<SaveLessonResponse> saveLessonProgress(@Valid SaveLessonRequest request) {
    var response = saveLessonProgressUseCase.execute(request);

    if (response.success()) {
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.badRequest().body(response);
    }
  }

  @GetMapping("/course/{courseId}/user/{userId}")
  public ResponseEntity<GetCourseProgressResponse> getCourseProgress(
      @PathVariable UUID courseId,
      @PathVariable UUID userId) {
    var response = getCourseProgressUseCase.execute(userId, courseId);
    return ResponseEntity.ok(response);
  }
}
