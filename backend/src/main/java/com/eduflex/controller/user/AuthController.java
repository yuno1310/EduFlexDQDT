package com.eduflex.controller.user;

import com.eduflex.dto.user.LogInDTO.LogoutRequest;
import com.eduflex.dto.user.LogInDTO.LogoutResponse;
import com.eduflex.dto.user.LogInDTO.RefreshTokenRequest;
import com.eduflex.dto.user.LogInDTO.RefreshTokenResponse;
import com.eduflex.repository.user.UserRepository;
import com.eduflex.security.JwtUtils;
import com.eduflex.security.RefreshTokenService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("api/auth")
public class AuthController {

  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private RefreshTokenService refreshTokenService;

  @Autowired
  private UserRepository userRepository;

  /**
   * Refresh an expired access token using a valid refresh token.
   * POST /api/auth/refresh
   */
  @PostMapping("/refresh")
  public ResponseEntity<RefreshTokenResponse> refreshToken(
      @Valid @RequestBody RefreshTokenRequest request) {

    String refreshToken = request.refreshToken();

    // Extract userId from refresh token to get user info
    UUID userId = jwtUtils.getUserIdFromJWT(refreshToken);
    if (userId == null) {
      return ResponseEntity.badRequest()
          .body(new RefreshTokenResponse(false, "Invalid refresh token", null));
    }

    var user = userRepository.find_by_id(userId);
    if (user == null) {
      return ResponseEntity.badRequest()
          .body(new RefreshTokenResponse(false, "User not found", null));
    }

    String role = user.record.getRole() != null ? user.record.getRole() : "user";
    String newAccessToken = refreshTokenService.refreshAccessToken(
        refreshToken, user.record.getEmail(), role);

    if (newAccessToken == null) {
      return ResponseEntity.status(401)
          .body(new RefreshTokenResponse(false, "Refresh token expired or revoked", null));
    }

    return ResponseEntity.ok(
        new RefreshTokenResponse(true, "Token refreshed successfully", newAccessToken));
  }

  /**
   * Logout — revoke a specific refresh token.
   * POST /api/auth/logout
   */
  @PostMapping("/logout")
  public ResponseEntity<LogoutResponse> logout(
      @Valid @RequestBody LogoutRequest request) {

    refreshTokenService.revokeRefreshToken(request.refreshToken());
    return ResponseEntity.ok(new LogoutResponse(true, "Logged out successfully"));
  }
}
