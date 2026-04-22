package com.eduflex.service;

import com.eduflex.dto.ReviewDTO.SubmitReviewRequest;
import com.eduflex.dto.ReviewDTO.SubmitReviewResponse;
import com.eduflex.repository.CourseReviewRepository;
import com.eduflex.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubmitReviewUseCase {

  @Autowired
  private CourseReviewRepository courseReviewRepository;

  @Autowired
  private EnrollmentRepository enrollmentRepository;

  public SubmitReviewResponse execute(SubmitReviewRequest request) {

    if (request.courseId() == null) {
      return new SubmitReviewResponse(false, "Course ID is missing!");
    }
    if (request.userId() == null) {
      return new SubmitReviewResponse(false, "User ID is missing!");
    }
    if (request.rating() == null || request.rating() < 1 || request.rating() > 5) {
      return new SubmitReviewResponse(false, "Rating must be between 1 and 5 stars!");
    }
    if (request.comment() == null || request.comment().trim().isEmpty()) {
      return new SubmitReviewResponse(false, "Please enter your review comment!");
    }

    boolean isEnrolled = enrollmentRepository.isUserEnrolled(request.userId(), request.courseId());
    if (!isEnrolled) {
      return new SubmitReviewResponse(false, "You must enroll in this course before leaving a review!");
    }

    try {
      courseReviewRepository.upsertReview(
          request.userId(),
          request.courseId(),
          request.rating(),
          request.comment());
      return new SubmitReviewResponse(true, "Review saved successfully!");
    } catch (Exception e) {
      return new SubmitReviewResponse(false, "System error: Unable to save review.");
    }
  }
}
