package com.eduflex.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.eduflex.dto.CourseSearchDTO.CourseSuggestionResponse;
import com.eduflex.dto.CreateCourseDTO.CreateCourseRequest;
import com.eduflex.dto.CreateCourseDTO.CreateCourseResponse;
import com.eduflex.dto.GetCourseDTO.GetCourseResponse;
import com.eduflex.dto.PaymentDTO.ProcessPaymentRequest;
import com.eduflex.dto.PaymentDTO.ProcessPaymentResponse;
import com.eduflex.dto.ReviewDTO.SubmitReviewRequest;
import com.eduflex.dto.ReviewDTO.SubmitReviewResponse;
import com.eduflex.service.CreateCourseUseCase;
import com.eduflex.service.GetCourseUseCase;
import com.eduflex.service.GetMyCoursesUseCase;
import com.eduflex.service.ProcessPaymentUseCase;
import com.eduflex.service.SearchCourseUseCase;
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

  @Autowired
  private SearchCourseUseCase searchCourseUseCase;

  @Autowired
  private GetMyCoursesUseCase getMyCoursesUseCase;

  @PostMapping
  public ResponseEntity<CreateCourseResponse> createCourse(
      @RequestBody CreateCourseRequest request) {

    var response = createCourseUseCase.execute(request);
    return response.sucess()
        ? ResponseEntity.ok(response)
        : ResponseEntity.badRequest().body(response);
  }

  @GetMapping
  public ResponseEntity<GetCourseResponse> getListCourse() {
    var response = getCourseUseCase.execute();
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
}
