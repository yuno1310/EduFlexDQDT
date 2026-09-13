package com.eduflex.service.enrollment;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eduflex.dto.enrollment.EnrolledCoursesDTO.EnrolledCourseInfo;
import com.eduflex.dto.enrollment.EnrolledCoursesDTO.GetEnrolledCoursesResponse;
import com.eduflex.repository.enrollment.EnrollmentRepository;

@Service
public class GetEnrolledCoursesUseCase {

  @Autowired
  private EnrollmentRepository enrollmentRepository;

  public GetEnrolledCoursesResponse execute(UUID userId) {
    List<EnrolledCourseInfo> courses = enrollmentRepository.getEnrolledCourses(userId);
    if (courses != null) {
      return new GetEnrolledCoursesResponse(true, "Enrolled courses retrieved.", courses);
    } else {
      return new GetEnrolledCoursesResponse(false, "Failed to retrieve enrolled courses.", List.of());
    }
  }
}
