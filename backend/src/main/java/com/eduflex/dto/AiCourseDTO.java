package com.eduflex.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AiCourseDTO {
  private AiCourseDTO() {}

  public record CourseSummaryResponse(UUID courseId, String summary, boolean generatedByAi) {}

  public record AskCourseRequest(
      @NotBlank @Size(max = 500) String question) {}

  public record AskCourseResponse(String answer, List<Source> sources, boolean generatedByAi) {}

  public record Source(UUID lessonId, String lessonTitle, double relevance) {}

  public record CourseMaterial(UUID courseId, String title, String description, String learningModel) {}

  public record LessonContext(UUID lessonId, String title, String content, double relevance) {}
}
