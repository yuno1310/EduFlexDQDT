package com.eduflex.dto;

import java.util.List;
import java.util.UUID;

public class GetLessonDTO {
  public record GetLessonRequest(UUID courseID) {
  }

  public record GetLessonResponse(boolean success, String message, List<LessonInfo> listLesson) {
  }

  public record LessonInfo(UUID lessonID, String title, String contentType) {
  }
}
