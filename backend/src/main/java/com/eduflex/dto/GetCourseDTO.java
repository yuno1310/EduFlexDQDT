package com.eduflex.dto;

import java.util.List;

public class GetCourseDTO {
  public record GetCourseRequest() {
  }

  public record GetCourseResponse(boolean success, String message, List<CourseInfo> listCourse) {
  }

  public record CourseInfo(String title, String learningMode, String status) {
  }
}
