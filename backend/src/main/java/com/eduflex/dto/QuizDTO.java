package com.eduflex.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public class QuizDTO {

  // === Submit Quiz (batch) ===

  public record AnswerItem(
      @NotNull Long questionId,
      @NotNull Long selectedOptionId) {
  }

  public record SubmitQuizRequest(
      @NotNull UUID userId,
      @NotNull UUID lessonId,
      @NotEmpty List<AnswerItem> answers) {
  }

  public record SubmitQuizResponse(
      boolean passed,
      String message,
      int correctCount,
      int totalQuestions,
      double scorePercent,
      Double courseProgress,
      boolean isCourseCompleted,
      int xpRewarded) {
  }

  // === Create Quiz (CRUD) ===

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

  // === Get Quiz ===

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
