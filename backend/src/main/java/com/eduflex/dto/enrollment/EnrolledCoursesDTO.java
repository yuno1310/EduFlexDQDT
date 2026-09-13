package com.eduflex.dto.enrollment;

import java.util.List;
import java.util.UUID;

public class EnrolledCoursesDTO {
  public record GetEnrolledCoursesResponse(boolean success, String message,
      List<EnrolledCourseInfo> enrolledCourses) {
  }

  public record EnrolledCourseInfo(UUID courseId, String title, String learningMode,
      double progressPercent) {
  }
}
