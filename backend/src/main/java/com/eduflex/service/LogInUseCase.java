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

  @Autowired
  private GetGamificationStatsUseCase getGamificationStatsUseCase;

  @Autowired
  private CheckAndAwardBadgesUseCase checkAndAwardBadgesUseCase;

  @Autowired
  private DailyCheckinUseCase dailyCheckinUseCase;

  public LogInResponse execute(LogInRequest request) {
    var user = userRepository.find_by_email(request.email());
    if (user == null) {
      return new LogInResponse(false, "User not existed", null, null, null, null);
    }
    boolean isMatch = passwordEncoder.matches(request.password(), user.record.getPasswordHash());
    if (isMatch == false) {
      return new LogInResponse(false, "Password is incorrect", null, null, null, null);
    }
    String token = jwtUtils.generateToken(user.record.getUserId(), user.record.getEmail());

    var userId = user.record.getUserId();

    // Ensure gamification_stats row exists
    getGamificationStatsUseCase.execute(userId);

    // Auto-award FIRST_LOGIN badge
    checkAndAwardBadgesUseCase.checkLoginBadge(userId);

    // Award daily login XP (+10, once per day)
    dailyCheckinUseCase.execute(userId);

    String role = user.record.getRole();
    if (role == null) role = "user";

    return new LogInResponse(true, "Log in successfully", token, role, user.record.getFullName(), user.record.getEmail());
  }
}

