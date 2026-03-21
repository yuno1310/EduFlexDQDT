package com.eduflex.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.eduflex.dto.LogInDTO.LogInRequest;
import com.eduflex.dto.LogInDTO.LogInResponse;
import com.eduflex.repository.UserRepository;

@Service
public class LogInUseCase {
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  public LogInResponse execute(LogInRequest request) {
    var user = userRepository.find_by_email(request.email());
    if (user == null) {
      return new LogInResponse(false, "User not existed");
    }
    boolean isMatch = passwordEncoder.matches(request.password(), user.record.getPasswordHash());
    if (isMatch == false) {
      return new LogInResponse(false, "Password is incorrect");
    }
    return new LogInResponse(true, "Log in successfully");
  }
}
