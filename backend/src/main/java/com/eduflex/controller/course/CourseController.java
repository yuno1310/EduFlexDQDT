package com.eduflex.controller.course;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eduflex.dto.course.CourseReviewDTO.GetCourseReviewsResponse;
import com.eduflex.dto.course.CourseReviewDTO.SubmitCourseReviewRequest;
import com.eduflex.dto.course.CourseReviewDTO.SubmitCourseReviewResponse;
import com.eduflex.dto.course.CourseSearchDTO.CourseSuggestionResponse;
import com.eduflex.dto.course.CreateCourseDTO.CreateCourseRequest;
import com.eduflex.dto.course.CreateCourseDTO.CreateCourseResponse;
import com.eduflex.dto.course.GetCourseDTO.GetCourseResponse;
import com.eduflex.dto.payment.PaymentDTO.ProcessPaymentRequest;
import com.eduflex.dto.payment.PaymentDTO.ProcessPaymentResponse;
import com.eduflex.dto.course.ReviewDTO.SubmitReviewRequest;
import com.eduflex.dto.course.ReviewDTO.SubmitReviewResponse;
import com.eduflex.service.course.CreateCourseUseCase;
import com.eduflex.service.course.GetCourseReviewsUseCase;
import com.eduflex.service.course.GetCourseUseCase;
import com.eduflex.service.course.GetMyCoursesUseCase;
import com.eduflex.service.payment.ProcessPaymentUseCase;
import com.eduflex.service.course.SearchCourseUseCase;
import com.eduflex.service.course.SubmitCourseReviewUseCase;
import com.eduflex.service.course.SubmitReviewUseCase;

import jakarta.validation.Valid;

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

  @Autowired
  private SearchCourseUseCase searchCourseUseCase;

  @Autowired
  private GetMyCoursesUseCase getMyCoursesUseCase;

  @Autowired
  private SubmitCourseReviewUseCase submitCourseReviewUseCase;

  @Autowired
  private GetCourseReviewsUseCase getCourseReviewsUseCase;

  @PostMapping
  public ResponseEntity<CreateCourseResponse> createCourse(
      @RequestBody CreateCourseRequest request) {

    var response = createCourseUseCase.execute(request);
    return response.success()
        ? ResponseEntity.ok(response)
        : ResponseEntity.badRequest().body(response);
  }

  @GetMapping
  public ResponseEntity<GetCourseResponse> getListCourse(Authentication authentication) {
    Object principal = authentication != null ? authentication.getPrincipal() : null;
    UUID userId = (principal instanceof UUID) ? (UUID) principal : null;
    var response = getCourseUseCase.execute(userId);
    return response.success()
        ? ResponseEntity.ok(response)
        : ResponseEntity.badRequest().body(response);
  }

  @PostMapping("/payment")
  public ResponseEntity<ProcessPaymentResponse> processPayment(
      @RequestBody ProcessPaymentRequest request) {

    var response = paymentUseCase.execute(request);
    return response.success()
        ? ResponseEntity.ok(response)
        : ResponseEntity.badRequest().body(response);
  }

  @PostMapping("/reviews")
  public ResponseEntity<SubmitReviewResponse> submitReview(
      @RequestBody SubmitReviewRequest request) {
    var response = submitReviewUseCase.execute(request);
    return response.success()
        ? ResponseEntity.ok(response)
        : ResponseEntity.badRequest().body(response);
  }

  @GetMapping("/search")
  public ResponseEntity<List<CourseSuggestionResponse>> searchCourses(
      @RequestHeader("X-User-Id") UUID userId,
      @RequestParam(name = "keyword", defaultValue = "") String keyword) {
    List<CourseSuggestionResponse> suggestions = searchCourseUseCase.execute(userId, keyword);
    return ResponseEntity.ok(suggestions);
  }

  @GetMapping("/my-courses")
  public ResponseEntity<List<CourseSuggestionResponse>> getMyCourses(
      @RequestHeader("X-User-Id") UUID userId) {
    List<CourseSuggestionResponse> myCourses = getMyCoursesUseCase.execute(userId);
    return ResponseEntity.ok(myCourses);
  }

  @GetMapping("/{courseId}/reviews")
  public ResponseEntity<GetCourseReviewsResponse> getCourseReviews(@PathVariable UUID courseId) {
    var response = getCourseReviewsUseCase.execute(courseId);
    if (response.success()) {
      return ResponseEntity.ok(response);
    }
    return ResponseEntity.badRequest().body(response);
  }

  @PostMapping("/{courseId}/reviews")
  public ResponseEntity<SubmitCourseReviewResponse> submitCourseReview(
      @PathVariable UUID courseId,
      @Valid @RequestBody SubmitCourseReviewRequest request,
      Authentication authentication) {
    Object principal = authentication != null ? authentication.getPrincipal() : null;
    if (!(principal instanceof UUID userId)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new SubmitCourseReviewResponse(false, "Unauthorized."));
    }

    var response = submitCourseReviewUseCase.execute(userId, courseId, request);
    if (response.success()) {
      return ResponseEntity.ok(response);
    }
    return ResponseEntity.badRequest().body(response);
  }
}
