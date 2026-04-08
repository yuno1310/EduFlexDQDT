package com.eduflex.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduflex.dto.PaymentDTO.ProcessPaymentRequest;
import com.eduflex.dto.PaymentDTO.ProcessPaymentResponse;
import com.eduflex.dto.EnrollmentDTO.EnrollRequest;
import com.eduflex.repository.CourseRepository;
import com.eduflex.repository.TransactionRepository;

@Service
public class ProcessPaymentUseCase {

  @Autowired
  private CourseRepository courseRepository;

  @Autowired
  private TransactionRepository transactionRepository;

  @Autowired
  private EnrollCourseUseCase enrollCourseUseCase;

  @Transactional
  public ProcessPaymentResponse execute(ProcessPaymentRequest request) {
    var courseRecord = courseRepository.find_by_id_course(request.courseId());
    if (courseRecord == null) {
      return new ProcessPaymentResponse(false, "This course does not exist in Database");
    }

    long amount = courseRecord.getPrice() != null ? courseRecord.getPrice() : 0;

    boolean isTransactionSaved = transactionRepository.saveSuccessfulTransaction(
        request.userId(),
        request.courseId(),
        amount);

    if (!isTransactionSaved) {
      return new ProcessPaymentResponse(false, "Failed to create payment");
    }

    EnrollRequest enrollReq = new EnrollRequest(request.userId(), request.courseId());
    var enrollResponse = enrollCourseUseCase.execute(enrollReq);

    if (enrollResponse.success()) {
      return new ProcessPaymentResponse(true, "Purchase successfully, you are added to course");
    } else {
      return new ProcessPaymentResponse(false, enrollResponse.message());
    }
  }
}
