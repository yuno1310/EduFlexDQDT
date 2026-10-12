package com.eduflex.dto.quiz;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.util.UUID;

public class FillBlankDTO {

  public record BlankAnswer(
      @NotNull Long questionId,
      @NotNull String submittedWord) {
  }

  public record SubmitFillBlankRequest(
      @NotNull UUID userId,
      @NotNull UUID lessonId,
      @NotEmpty List<@NotNull @Valid BlankAnswer> answers) {
  }

  public record FillBlankResultDetail(
      Long questionId,
      boolean isCorrect,
      String correctAnswer) {
  }

  public record SubmitFillBlankResponse(
      boolean success,
      String message,
      int correctCount,
      int totalQuestions,
      List<FillBlankResultDetail> details) {
  }
}
