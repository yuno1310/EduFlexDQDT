package com.eduflex.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.eduflex.dto.user.LogInDTO.LogInRequest;
import com.eduflex.dto.user.LogInDTO.LogInResponse;
import com.eduflex.repository.user.UserRepository;
import com.eduflex.security.JwtUtils;
import com.eduflex.security.RefreshTokenService;
import com.eduflex.service.gamification.GetGamificationStatsUseCase;
import com.eduflex.service.gamification.CheckAndAwardBadgesUseCase;
import com.eduflex.service.gamification.DailyCheckinUseCase;

@Service
public class LogInUseCase {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private RefreshTokenService refreshTokenService;

  @Autowired
  private GetGamificationStatsUseCase getGamificationStatsUseCase;

  @Autowired
  private CheckAndAwardBadgesUseCase checkAndAwardBadgesUseCase;

  @Autowired
  private DailyCheckinUseCase dailyCheckinUseCase;

  public LogInResponse execute(LogInRequest request) {
    var user = userRepository.find_by_email(request.email());
    if (user == null) {
      return new LogInResponse(false, "User not existed", null, null, null, null, null, null);
    }
    boolean isMatch = passwordEncoder.matches(request.password(), user.record.getPasswordHash());
    if (!isMatch) {
      return new LogInResponse(false, "Password is incorrect", null, null, null, null, null, null);
    }

    var userId = user.record.getUserId();
    String role = user.record.getRole();
    if (role == null) role = "user";

    // Generate access token (15 min) + refresh token (30 days, stored in Redis)
    String accessToken = jwtUtils.generateAccessToken(userId, user.record.getEmail(), role);
    String refreshToken = refreshTokenService.createRefreshToken(userId);

    // Ensure gamification_stats row exists
    getGamificationStatsUseCase.execute(userId);

    // Auto-award FIRST_LOGIN badge
    checkAndAwardBadgesUseCase.checkLoginBadge(userId);

    // Award daily login XP (+10, once per day)
    dailyCheckinUseCase.execute(userId);

    return new LogInResponse(true, "Log in successfully", accessToken, refreshToken,
        role, user.record.getFullName(), user.record.getEmail(), user.record.getAvatarUrl());
  }
}
