package com.eduflex.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;

public class PaymentDTO {

  public record ProcessPaymentRequest(
      @JsonAlias( {
          "userId", "user_id", "userid" }) UUID userId,
      @JsonAlias({ "courseId", "course_id", "courseid" }) UUID courseId){
  }

  public record ProcessPaymentResponse(
      boolean success,
      String message) {
  }
}
