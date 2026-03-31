package com.eduflex.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public class QuestionDTO {
  public record CreateQuestionRequest(@NotNull UUID lessonId, @NotNull String questionText, Integer points) {
  }

  public record CreateQuestionResponse(boolean success, String message) {
  }

  public record CreateOptionRequest(@NotNull Long questionId, @NotNull String optionText, @NotNull Boolean isCorrect) {
  }

  public record CreateOptionResponse(boolean success, String message) {
  }
}
