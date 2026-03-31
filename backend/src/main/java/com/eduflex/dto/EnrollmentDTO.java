package com.eduflex.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public class EnrollmentDTO {
  public record EnrollRequest(@NotNull UUID userId, @NotNull UUID courseId) {
  }

  public record EnrollResponse(boolean success, String message) {
  }
}
