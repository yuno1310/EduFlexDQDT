package com.eduflex.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eduflex.dto.CourseReviewDTO.SubmitCourseReviewRequest;
import com.eduflex.dto.CourseReviewDTO.SubmitCourseReviewResponse;
import com.eduflex.repository.CourseRepository;
import com.eduflex.repository.CourseReviewRepository;

@Service
public class SubmitCourseReviewUseCase {

  @Autowired
  private CourseRepository courseRepository;

  @Autowired
  private CourseReviewRepository courseReviewRepository;

  public SubmitCourseReviewResponse execute(UUID userId, UUID courseId, SubmitCourseReviewRequest request) {
    if (!courseRepository.existsById(courseId)) {
      return new SubmitCourseReviewResponse(false, "Course not found.");
    }

    int rating = request.rating();
    if (rating < 1 || rating > 5) {
      return new SubmitCourseReviewResponse(false, "Rating must be between 1 and 5.");
    }

    String comment = request.comment() == null ? "" : request.comment().trim();
    if (comment.isEmpty()) {
      return new SubmitCourseReviewResponse(false, "Comment is required.");
    }

    boolean saved = courseReviewRepository.upsertReview(userId, courseId, rating, comment);
    if (!saved) {
      return new SubmitCourseReviewResponse(false, "Failed to submit review.");
    }

    return new SubmitCourseReviewResponse(true, "Review submitted.");
  }
}
