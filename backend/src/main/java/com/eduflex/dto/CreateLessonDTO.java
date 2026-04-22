package com.eduflex.dto;

import java.util.UUID;

public class CreateLessonDTO {
  public record CreateLessonRequest(UUID courseID, String title, String contentType, String content) {
  }

  public record CreateLessonResponse(boolean success, String message) {
  }
}
