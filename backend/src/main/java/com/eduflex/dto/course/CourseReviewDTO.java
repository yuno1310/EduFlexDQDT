package com.eduflex.dto.course;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CourseReviewDTO {

  public record SubmitCourseReviewRequest(
      @NotNull @Min(1) @Max(5) Integer rating,
      String comment) {
  }

  public record CourseReviewInfo(
      UUID userId,
      String reviewerName,
      int rating,
      String comment,
      String createdAt) {
  }

  public record SubmitCourseReviewResponse(
      boolean success,
      String message) {
  }

  public record GetCourseReviewsResponse(
      boolean success,
      String message,
      List<CourseReviewInfo> reviews) {
  }
}
