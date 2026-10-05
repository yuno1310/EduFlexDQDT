package com.eduflex.service.course;

import com.eduflex.dto.course.RegisterCourseDTO.RegisterRequest;
import com.eduflex.dto.course.RegisterCourseDTO.RegisterResponse;
import com.eduflex.repository.enrollment.EnrollmentRepository;
import com.eduflex.repository.course.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class RegisterCourseUseCase {

  @Autowired
  private EnrollmentRepository enrollmentRepository;

  @Autowired
  private CourseRepository courseRepository;

  public RegisterResponse execute(UUID courseId, RegisterRequest request) {
    if (request.userId() == null) {
      return new RegisterResponse(false, "User ID not found!");
    }
    var course = courseRepository.find_by_id_course(courseId);
    if (course == null) {
      return new RegisterResponse(false, "Course not found.");
    }
    if (course.getPrice() != null && course.getPrice() > 0) {
      return new RegisterResponse(false, "Paid courses must use the checkout flow.");
    }
    boolean isAlreadyRegistered = enrollmentRepository.isUserEnrolled(request.userId(), courseId);
    if (isAlreadyRegistered) {
      return new RegisterResponse(false, "You have already registered for this course!");
    }
    try {
      enrollmentRepository.enrollUser(request.userId(), courseId);
      return new RegisterResponse(true, "Course registration successful!");
    } catch (Exception e) {
      return new RegisterResponse(false, "System error: Unable to complete registration.");
    }
  }
}
