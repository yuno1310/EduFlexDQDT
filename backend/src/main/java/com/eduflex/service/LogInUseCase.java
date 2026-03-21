package com.eduflex.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.eduflex.dto.LogInDTO.LogInRequest;
import com.eduflex.dto.LogInDTO.LogInResponse;
import com.eduflex.repository.UserRepository;
import com.eduflex.security.JwtUtils;

@Service
public class LogInUseCase {
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JwtUtils jwtUtils;

  public LogInResponse execute(LogInRequest request) {
    var user = userRepository.find_by_email(request.email());
    if (user == null) {
      return new LogInResponse(false, "User not existed", null);
    }
    boolean isMatch = passwordEncoder.matches(request.password(), user.record.getPasswordHash());
    if (isMatch == false) {
      return new LogInResponse(false, "Password is incorrect", null);
    }
    String token = jwtUtils.generateToken(user.record.getUserId(), user.record.getEmail());
    return new LogInResponse(true, "Log in successfully", token);
  }
}
