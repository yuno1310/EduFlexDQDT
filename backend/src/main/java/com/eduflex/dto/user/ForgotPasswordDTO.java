package com.eduflex.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ForgotPasswordDTO {
    public record ForgotPasswordRequest(
        @JsonProperty("email") String email,
        @JsonProperty("newPassword") String newPassword
    ) {}

    public record ForgotPasswordResponse(boolean success, String message) {}
}
