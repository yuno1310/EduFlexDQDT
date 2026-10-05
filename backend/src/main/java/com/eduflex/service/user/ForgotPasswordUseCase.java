package com.eduflex.service.user;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduflex.dto.user.ForgotPasswordDTO.ForgotPasswordRequest;
import com.eduflex.dto.user.ForgotPasswordDTO.ForgotPasswordResponse;
import com.eduflex.dto.user.ForgotPasswordDTO.ResetPasswordRequest;
import com.eduflex.repository.user.PasswordResetTokenRepository;
import com.eduflex.repository.user.UserRepository;
import com.eduflex.security.RefreshTokenService;

@Service
public class ForgotPasswordUseCase {
  private static final String GENERIC_MESSAGE =
      "If the account exists, a single-use reset code has been sent.";

  private final UserRepository users;
  private final PasswordResetTokenRepository tokens;
  private final PasswordEncoder passwordEncoder;
  private final RefreshTokenService refreshTokens;
  private final PasswordResetDelivery delivery;
  private final long ttlMinutes;
  private final SecureRandom random = new SecureRandom();
  private final Clock clock = Clock.systemUTC();

  public ForgotPasswordUseCase(UserRepository users, PasswordResetTokenRepository tokens,
      PasswordEncoder passwordEncoder, RefreshTokenService refreshTokens,
      ObjectProvider<PasswordResetDelivery> deliveryProvider,
      @Value("${eduflex.password-reset.ttl-minutes:15}") long ttlMinutes) {
    this.users = users;
    this.tokens = tokens;
    this.passwordEncoder = passwordEncoder;
    this.refreshTokens = refreshTokens;
    this.delivery = deliveryProvider.getIfAvailable();
    this.ttlMinutes = ttlMinutes;
  }

  @Transactional
  public ForgotPasswordResponse request(ForgotPasswordRequest request) {
    if (delivery == null) {
      return new ForgotPasswordResponse(false,
          "Password recovery is unavailable until email delivery is configured.");
    }
    var user = users.find_by_email(request.email().trim().toLowerCase());
    if (user != null) {
      String token = generateToken();
      tokens.replaceForUser(user.record.getUserId(), hash(token),
          LocalDateTime.now(clock).plusMinutes(ttlMinutes));
      delivery.send(user.record.getEmail(), token, ttlMinutes);
    }
    return new ForgotPasswordResponse(true, GENERIC_MESSAGE);
  }

  @Transactional
  public ForgotPasswordResponse confirm(ResetPasswordRequest request) {
    UUID userId = tokens.consume(hash(request.token().trim()), LocalDateTime.now(clock));
    if (userId == null) {
      return new ForgotPasswordResponse(false, "The reset code is invalid or expired.");
    }
    if (!users.updatePasswordById(userId, passwordEncoder.encode(request.newPassword()))) {
      throw new IllegalStateException("Password reset target disappeared");
    }
    refreshTokens.revokeAllTokens(userId);
    return new ForgotPasswordResponse(true, "Password updated. Sign in with the new password.");
  }

  private String generateToken() {
    byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String hash(String token) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
          .digest(token.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException impossible) {
      throw new IllegalStateException(impossible);
    }
  }
}
