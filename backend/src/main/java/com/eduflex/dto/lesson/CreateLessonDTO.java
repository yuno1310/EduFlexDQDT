package com.eduflex.dto.lesson;

import java.util.UUID;

public class CreateLessonDTO {
  public record CreateLessonRequest(UUID courseID, String title, String contentType, String videoUrl, String content) {
  }

  public record CreateLessonResponse(boolean success, String message, UUID lessonId) {
  }
}
