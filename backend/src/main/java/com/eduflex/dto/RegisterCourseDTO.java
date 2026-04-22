package com.eduflex.dto;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonAlias;

public class RegisterCourseDTO {
  public record RegisterRequest(
      @JsonAlias( {
          "userId", "user_id" }) UUID userId){
  }

  public record RegisterResponse(boolean success, String message) {
  }
}
