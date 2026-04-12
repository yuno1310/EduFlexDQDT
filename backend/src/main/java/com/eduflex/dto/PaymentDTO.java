package com.eduflex.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PaymentDTO {
  public record PaymentRequest(@NotNull UUID userId, @NotBlank String paymentMethod) {
  }

  public record PaymentResponse(boolean success, String message) {
  }
}
