package com.eduflex.dto;

import jakarta.validation.constraints.NotNull;
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
}
