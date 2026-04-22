package com.eduflex.dto;

import java.util.UUID;

public class ReviewDTO {

  public record SubmitReviewRequest(
      UUID courseId,
      UUID userId,
      Integer rating,
      String comment) {
  }

  public record SubmitReviewResponse(
      boolean success,
      String message) {
  }
}
