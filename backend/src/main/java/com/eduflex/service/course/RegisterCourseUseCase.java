package com.eduflex.service.course;

import com.eduflex.dto.course.RegisterCourseDTO.RegisterRequest;
import com.eduflex.dto.course.RegisterCourseDTO.RegisterResponse;
import com.eduflex.repository.enrollment.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class RegisterCourseUseCase {

  @Autowired
  private EnrollmentRepository enrollmentRepository;

  public RegisterResponse execute(UUID courseId, RegisterRequest request) {
    if (request.userId() == null) {
      return new RegisterResponse(false, "User ID not found!");
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
