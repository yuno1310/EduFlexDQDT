package com.eduflex.dto;

import java.util.List;
import java.util.UUID;

public class GetCourseDTO {
  public record GetCourseRequest() {
  }

  public record GetCourseResponse(boolean success, String message, List<CourseInfo> listCourse) {
  }

  public record CourseInfo(UUID courseID, String title, String learningMode, String status) {
  }
}
