package com.eduflex.service.enrollment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eduflex.dto.enrollment.EnrollmentDTO.EnrollRequest;
import com.eduflex.dto.enrollment.EnrollmentDTO.EnrollResponse;
import com.eduflex.entity.enrollment.EnrollmentDbO;
import com.eduflex.repository.enrollment.EnrollmentRepository;

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
