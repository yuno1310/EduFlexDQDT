package com.eduflex.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LogInDTO {
  public record LogInRequest(@NotBlank @Email String email, @NotBlank String password) {
  }

  public record LogInResponse(boolean success, String message) {
  }
}
