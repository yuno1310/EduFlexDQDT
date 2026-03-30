package com.eduflex.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateUserDTO {
  public record CreateUserRequest(@NotBlank @Email String email,
      @NotBlank(message = "Mật khẩu không được để trống") @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự") @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$", message = "Mật khẩu yếu! Phải chứa ít nhất 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt (@#$%^&+=!)") String password,
      @NotEmpty String name,
      boolean active) {
  }

  public record CreateUserResponse(boolean success, String message) {
  }
}
