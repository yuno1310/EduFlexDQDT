package com.eduflex.service.user;

public interface PasswordResetDelivery {
  void send(String email, String token, long ttlMinutes);
}
