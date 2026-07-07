package com.eduflex.controller.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduflex.dto.payment.PaymentDTO.ProcessPaymentRequest;
import com.eduflex.dto.payment.PaymentDTO.ProcessPaymentResponse;
import com.eduflex.service.payment.ProcessPaymentUseCase;

@RestController
@RequestMapping("api/payment")
public class PaymentController {

  @Autowired
  private ProcessPaymentUseCase processPaymentUseCase;

  @PostMapping
  public ResponseEntity<ProcessPaymentResponse> processPayment(
      @RequestBody ProcessPaymentRequest request) {
    var response = processPaymentUseCase.execute(request);
    if (response.success()) {
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.badRequest().body(response);
    }
  }
}
