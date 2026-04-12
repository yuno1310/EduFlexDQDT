package com.eduflex.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduflex.dto.PaymentDTO.PaymentRequest;
import com.eduflex.dto.PaymentDTO.PaymentResponse;
import com.eduflex.service.ProcessPaymentUseCase;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/payment")
public class PaymentController {

  @Autowired
  private ProcessPaymentUseCase processPaymentUseCase;

  @PostMapping
  public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
    var response = processPaymentUseCase.execute(request);
    if (response.success()) {
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.badRequest().body(response);
    }
  }
}
