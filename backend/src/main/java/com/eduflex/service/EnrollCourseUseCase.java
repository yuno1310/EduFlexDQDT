package com.eduflex.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eduflex.dto.EnrollmentDTO.EnrollRequest;
import com.eduflex.dto.EnrollmentDTO.EnrollResponse;
import com.eduflex.entity.EnrollmentDbO;
import com.eduflex.repository.EnrollmentRepository;

@Service
public class EnrollCourseUseCase {
  @Autowired
  private EnrollmentRepository enrollmentRepository;

  public EnrollResponse execute(EnrollRequest request) {
    var enrollment = new EnrollmentDbO(request.userId(), request.courseId());
    if (enrollmentRepository.save(enrollment)) {
      return new EnrollResponse(true, "Successfully enrolled in the course!");
    } else {
      return new EnrollResponse(false, "Failed to enroll in the course.");
    }
  }
}
