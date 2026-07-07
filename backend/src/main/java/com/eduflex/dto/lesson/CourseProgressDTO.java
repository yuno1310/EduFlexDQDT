package com.eduflex.dto.lesson;

import java.util.List;
import java.util.UUID;

public class CourseProgressDTO {
  public record LessonProgressItem(
      UUID lessonId,
      String title,
      String contentType,
      boolean isCompleted,
      boolean isLocked) {
  }

  public record LessonProgressInfo(
      UUID lessonId,
      String title,
      String contentType,
      Boolean isCompleted) {
  }

  public record GetCourseProgressResponse(
      boolean success,
      String message,
      List<LessonProgressItem> lessons) {
  }

}
