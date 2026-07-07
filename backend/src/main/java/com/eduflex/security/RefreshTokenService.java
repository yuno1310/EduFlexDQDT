package com.eduflex.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Stateful refresh token management using Redis.
 * Each refresh token is stored with key: refresh:{userId}:{tokenId}
 * Supports single-token revocation and full user logout.
 */
@Service
public class RefreshTokenService {

  private static final String REFRESH_KEY_PREFIX = "refresh:";

  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private StringRedisTemplate redisTemplate;

  /**
   * Create and store a new refresh token for the user.
   * Returns the JWT refresh token string.
   */
  public String createRefreshToken(UUID userId) {
    String refreshToken = jwtUtils.generateRefreshToken(userId);
    String tokenId = jwtUtils.getTokenIdFromRefreshToken(refreshToken);

    if (tokenId == null) {
      throw new RuntimeException("Failed to extract tokenId from refresh token");
    }

    // Store in Redis with TTL matching the token expiry
    String key = buildKey(userId, tokenId);
    redisTemplate.opsForValue().set(key, "active",
        jwtUtils.getRefreshTokenExpirySeconds(), TimeUnit.SECONDS);

    return refreshToken;
  }

  /**
   * Validate a refresh token and issue a new access token.
   * Returns null if the token is invalid or revoked.
   */
  public String refreshAccessToken(String refreshToken, String email, String role) {
    if (!jwtUtils.isValidRefreshToken(refreshToken)) {
      return null;
    }

    UUID userId = jwtUtils.getUserIdFromJWT(refreshToken);
    String tokenId = jwtUtils.getTokenIdFromRefreshToken(refreshToken);

    if (userId == null || tokenId == null) {
      return null;
    }

    // Check if the refresh token is still active in Redis
    String key = buildKey(userId, tokenId);
    String status = redisTemplate.opsForValue().get(key);

    if (!"active".equals(status)) {
      return null; // Token has been revoked or expired
    }

    // Issue a new access token
    return jwtUtils.generateAccessToken(userId, email, role);
  }

  /**
   * Revoke a specific refresh token.
   */
  public void revokeRefreshToken(String refreshToken) {
    UUID userId = jwtUtils.getUserIdFromJWT(refreshToken);
    String tokenId = jwtUtils.getTokenIdFromRefreshToken(refreshToken);

    if (userId != null && tokenId != null) {
      String key = buildKey(userId, tokenId);
      redisTemplate.delete(key);
    }
  }

  /**
   * Revoke ALL refresh tokens for a user (force logout everywhere).
   */
  public void revokeAllTokens(UUID userId) {
    String pattern = REFRESH_KEY_PREFIX + userId.toString() + ":*";
    Set<String> keys = redisTemplate.keys(pattern);
    if (keys != null && !keys.isEmpty()) {
      redisTemplate.delete(keys);
    }
  }

  private String buildKey(UUID userId, String tokenId) {
    return REFRESH_KEY_PREFIX + userId.toString() + ":" + tokenId;
  }
}
