package com.eduflex.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduflex.dto.CreateCourseDTO.CreateCourseRequest;
import com.eduflex.dto.CreateCourseDTO.CreateCourseResponse;
import com.eduflex.dto.GetCourseDTO.GetCourseResponse;
import com.eduflex.dto.PaymentDTO.ProcessPaymentRequest;
import com.eduflex.dto.PaymentDTO.ProcessPaymentResponse;
import com.eduflex.dto.ReviewDTO.SubmitReviewRequest;
import com.eduflex.dto.ReviewDTO.SubmitReviewResponse;
import com.eduflex.service.CreateCourseUseCase;
import com.eduflex.service.GetCourseUseCase;
import com.eduflex.service.ProcessPaymentUseCase;
import com.eduflex.service.SubmitReviewUseCase;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("api/course")
public class CourseController {
  @Autowired
  private CreateCourseUseCase createCourseUseCase;

  @Autowired
  private GetCourseUseCase getCourseUseCase;

  @Autowired
  private ProcessPaymentUseCase paymentUseCase;

  @Autowired
  private SubmitReviewUseCase submitReviewUseCase;

  @PostMapping
  public ResponseEntity<CreateCourseResponse> createCourse(CreateCourseRequest request) {
    var response = createCourseUseCase.execute(request);
    if (response.sucess() == true) {
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.badRequest().body(response);
    }
  }

  @GetMapping
  public ResponseEntity<GetCourseResponse> getListCourse() {
    var response = getCourseUseCase.execute();
    if (response.success() == true) {
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.badRequest().body(response);
    }
  }

  @PostMapping("/payment")
  public ResponseEntity<ProcessPaymentResponse> processPayemnt(ProcessPaymentRequest request) {
    var response = paymentUseCase.execute(request);
    if (response.success() == true) {
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.badRequest().body(response);
    }
  }

  @PostMapping("/api/courses/{courseId}/reviews")
  public ResponseEntity<SubmitReviewResponse> submitReview(
      @PathVariable UUID courseId,
      @RequestBody SubmitReviewRequest request) {
    var response = submitReviewUseCase.execute(courseId, request);
    if (response.success()) {
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.badRequest().body(response);
    }
  }
}
