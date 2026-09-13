package com.eduflex.dto.course;

import jakarta.validation.constraints.NotEmpty;

public class CreateCourseDTO {
  public record CreateCourseRequest(
      @NotEmpty String title,
      @NotEmpty String learningModel,
      @NotEmpty String status,
      String description,
      String imageUrl,
      Long price) {
  }

  public record CreateCourseResponse(boolean success, String message) {
  }
}
