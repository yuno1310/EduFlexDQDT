package com.eduflex.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;

public class ReviewDTO {
  public record SubmitReviewRequest(@JsonAlias UUID userId, Integer rating, String comment) {
  }

  public record SubmitReviewResponse(boolean success, String message) {
  }
}
