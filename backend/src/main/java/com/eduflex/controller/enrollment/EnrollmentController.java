package com.eduflex.controller.enrollment;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduflex.dto.enrollment.EnrolledCoursesDTO.GetEnrolledCoursesResponse;
import com.eduflex.dto.course.RegisterCourseDTO.RegisterRequest;
import com.eduflex.dto.course.RegisterCourseDTO.RegisterResponse;
import com.eduflex.service.enrollment.GetEnrolledCoursesUseCase;
import com.eduflex.service.course.RegisterCourseUseCase;

@RestController
@RequestMapping("api/enrollment")
public class EnrollmentController {

  @Autowired
  private GetEnrolledCoursesUseCase getEnrolledCoursesUseCase;

  @Autowired
  private RegisterCourseUseCase registerCourseUseCase;

  @GetMapping("/{userId}")
  public ResponseEntity<GetEnrolledCoursesResponse> getEnrolledCourses(
      @PathVariable UUID userId) {
    var response = getEnrolledCoursesUseCase.execute(userId);
    if (response.success()) {
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.badRequest().body(response);
    }
  }

  @PostMapping("/{courseId}/register")
  public ResponseEntity<RegisterResponse> registerCourse(
      @PathVariable UUID courseId,
      @RequestBody RegisterRequest request) {
    var response = registerCourseUseCase.execute(courseId, request);
    if (response.success()) {
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.badRequest().body(response);
    }
  }
}
