package com.eduflex.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public class CreateUserDTO {
  public record CreateUserRequest(@NotBlank @Email String email, @NotEmpty String password, @NotEmpty String name,
      @NotEmpty boolean active) {
  }

  public record CreateUserResponse(boolean success, String message) {
  }
}
