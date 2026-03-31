package com.eduflex.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public class QuizDTO {

  public record SubmitQuizRequest(
      @NotNull UUID userId,
      @NotNull UUID lessonId,
      @NotNull Long selectedOptionId) {
  }

  public record SubmitQuizResponse(
      boolean isCorrect,
      String message,
      Double courseProgress,
      boolean isCourseCompleted,
      Integer xpRewarded) {
  }

  public record OptionRequest(@NotEmpty String optionText, @NotNull Boolean isCorrect) {
  }

  public record CreateQuizRequest(
      @NotNull UUID lessonId,
      @NotEmpty String questionText,
      int points,
      @NotNull List<OptionRequest> options) {
  }

  public record CreateQuizResponse(boolean success, String message) {
  }

  public record OptionResponse(Long optionId, String optionText) {
  }

  public record GetQuizResponse(
      boolean success,
      String message,
      Long questionId,
      String questionText,
      int points,
      List<OptionResponse> options) {
  }
}
