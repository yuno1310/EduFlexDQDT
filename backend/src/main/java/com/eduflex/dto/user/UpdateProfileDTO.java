package com.eduflex.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateProfileDTO {
  public record UpdateProfileRequest(@JsonProperty("fullName") String newFullName,
      @JsonProperty("newPassword") String newPassword) {
  }

  public record UpdateProfileResponse(boolean success, String message) {
  }
}
