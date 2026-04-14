package com.eduflex.dto;

import java.util.List;
import java.util.UUID;

public class FillBlankDTO {

  public record BlankAnswer(
      Long questionId,
      String submittedWord) {
  }

  public record SubmitFillBlankRequest(
      UUID userId,
      UUID lessonId,
      List<BlankAnswer> answers) {
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
