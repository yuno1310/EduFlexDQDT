package com.eduflex.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduflex.dto.EnrolledCoursesDTO.GetEnrolledCoursesResponse;
import com.eduflex.service.GetEnrolledCoursesUseCase;

@RestController
@RequestMapping("api/enrollment")
public class EnrollmentController {

  @Autowired
  private GetEnrolledCoursesUseCase getEnrolledCoursesUseCase;

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
}
