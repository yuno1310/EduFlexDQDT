package com.eduflex.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eduflex.dto.CourseReviewDTO.CourseReviewInfo;
import com.eduflex.dto.CourseReviewDTO.GetCourseReviewsResponse;
import com.eduflex.repository.CourseRepository;
import com.eduflex.repository.CourseReviewRepository;

@Service
public class GetCourseReviewsUseCase {

  private static final DateTimeFormatter REVIEW_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  @Autowired
  private CourseRepository courseRepository;

  @Autowired
  private CourseReviewRepository courseReviewRepository;

  public GetCourseReviewsResponse execute(UUID courseId) {
    if (!courseRepository.existsById(courseId)) {
      return new GetCourseReviewsResponse(false, "Course not found.", List.of());
    }

    var reviewRows = courseReviewRepository.getCourseReviews(courseId);
    List<CourseReviewInfo> reviews = new ArrayList<>();
    for (var row : reviewRows) {
      String createdAt = row.createdAt() != null ? row.createdAt().format(REVIEW_TIME_FORMAT) : "";
      reviews.add(new CourseReviewInfo(
          row.userId(),
          row.reviewerName(),
          row.rating(),
          row.comment(),
          createdAt));
    }
    return new GetCourseReviewsResponse(true, "Course reviews retrieved.", reviews);
  }
}
