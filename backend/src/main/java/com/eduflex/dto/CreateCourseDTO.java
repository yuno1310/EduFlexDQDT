package com.eduflex.dto;

import jakarta.validation.constraints.NotEmpty;

public class CreateCourseDTO {
  public record CreateCourseRequest(@NotEmpty String title, @NotEmpty String learning_mode, @NotEmpty String status) {
  }

  public record CreateCourseResponse(boolean sucess, String message) {
  }
}
