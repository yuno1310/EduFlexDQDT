package com.eduflex.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LogInDTO {
  public record LogInRequest(@NotBlank @Email String email, @NotBlank String password) {
  }

  public record LogInResponse(boolean success, String message, String accessToken,
      String refreshToken, String role, String fullName, String email, String avatarUrl) {
  }

  public record RefreshTokenRequest(@NotBlank String refreshToken) {
  }

  public record RefreshTokenResponse(boolean success, String message, String accessToken) {
  }

  public record LogoutRequest(@NotBlank String refreshToken) {
  }

  public record LogoutResponse(boolean success, String message) {
  }
}
