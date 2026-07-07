package com.eduflex.dto.lesson;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class ProgressDTO {

  public record SaveLessonRequest(
      @NotNull UUID lessonId,
      @NotNull UUID userId) {
  }

  public record SaveLessonResponse(
      boolean success,
      String message,
      Double newProgressPercent) {
  }
}
