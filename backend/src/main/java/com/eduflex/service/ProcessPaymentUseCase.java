package com.eduflex.service;

import org.springframework.stereotype.Service;

import com.eduflex.dto.PaymentDTO.PaymentRequest;
import com.eduflex.dto.PaymentDTO.PaymentResponse;

@Service
public class ProcessPaymentUseCase {

  public PaymentResponse execute(PaymentRequest request) {
    // Mock payment: always succeeds
    return new PaymentResponse(true, "Payment processed successfully!");
  }
}
