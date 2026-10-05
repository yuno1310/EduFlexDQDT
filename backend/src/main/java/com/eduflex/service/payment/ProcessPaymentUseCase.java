package com.eduflex.service.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduflex.dto.payment.PaymentDTO.ProcessPaymentRequest;
import com.eduflex.dto.payment.PaymentDTO.ProcessPaymentResponse;
import com.eduflex.repository.course.CourseRepository;
import com.eduflex.repository.enrollment.EnrollmentRepository;
import com.eduflex.repository.payment.TransactionRepository;

@Service
public class ProcessPaymentUseCase {

  @Autowired
  private CourseRepository courseRepository;

  @Autowired
  private TransactionRepository transactionRepository;

  @Autowired
  private EnrollmentRepository enrollmentRepository;

  @Value("${eduflex.payment.simulation-enabled:false}")
  private boolean simulationEnabled;

  @Transactional
  public ProcessPaymentResponse execute(ProcessPaymentRequest request) {
    if (!simulationEnabled) {
      return new ProcessPaymentResponse(false,
          "Development payment simulation is disabled.", true);
    }
    var courseRecord = courseRepository.find_by_id_course(request.courseId());
    if (courseRecord == null) {
      return new ProcessPaymentResponse(false, "Course not found.", true);
    }

    long amount = courseRecord.getPrice() != null ? courseRecord.getPrice() : 0;

    boolean isTransactionSaved = transactionRepository.saveSuccessfulTransaction(
        request.userId(),
        request.courseId(),
        amount);

    if (!isTransactionSaved
        && !transactionRepository.hasSuccessfulTransaction(request.userId(), request.courseId())) {
      return new ProcessPaymentResponse(false, "Failed to create payment record.", true);
    }

    if (!enrollmentRepository.isUserEnrolled(request.userId(), request.courseId())) {
      enrollmentRepository.enrollUser(request.userId(), request.courseId());
    }
    return new ProcessPaymentResponse(true,
        "Simulation completed; no real payment method was charged.", true);
  }
}
