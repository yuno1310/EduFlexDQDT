package com.eduflex.service;

import com.eduflex.dto.AddXpDTO;
import com.eduflex.repository.GamificationStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.eduflex.dto.LogInDTO.LogInRequest;
import com.eduflex.dto.LogInDTO.LogInResponse;
import com.eduflex.repository.UserRepository;
import com.eduflex.security.JwtUtils;

import java.time.LocalDate;

@Service
public class LogInUseCase {

  private static final int DAILY_LOGIN_XP = 10;

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
  private GamificationStatsRepository gamificationStatsRepository;

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

    var userId = user.record.getUserId();

    // Ensure gamification_stats row exists
    var stats = getGamificationStatsUseCase.execute(userId);

    // Auto-award FIRST_LOGIN badge
    checkAndAwardBadgesUseCase.checkLoginBadge(userId);

    // Award daily login XP (only once per day, does NOT update streak)
    // Streak only increases when user completes a lesson/quiz
    LocalDate today = LocalDate.now();
    LocalDate lastLoginXpDate = gamificationStatsRepository.getLastLoginXpDate(userId);
    if (lastLoginXpDate == null || !lastLoginXpDate.equals(today)) {
      gamificationStatsRepository.updateXpAndLevel(userId, DAILY_LOGIN_XP);
      gamificationStatsRepository.setLastLoginXpDate(userId, today);
    }

    return new LogInResponse(true, "Log in successfully", token);
  }
}
