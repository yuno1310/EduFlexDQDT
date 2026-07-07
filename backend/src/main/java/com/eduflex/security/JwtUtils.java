package com.eduflex.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtils {

  @Value("${JwtSecret}")
  private String jwtSecret;

  private static final long ACCESS_TOKEN_EXPIRY_MS = 15 * 60 * 1000; // 15 minutes
  private static final long REFRESH_TOKEN_EXPIRY_MS = 30L * 24 * 60 * 60 * 1000; // 30 days

  private static final String ISSUER = "EduFlexApp";
  private static final String CLAIM_TOKEN_TYPE = "type";
  private static final String CLAIM_EMAIL = "email";
  private static final String CLAIM_ROLE = "role";
  private static final String CLAIM_TOKEN_ID = "jti";
  private static final String TYPE_ACCESS = "access";
  private static final String TYPE_REFRESH = "refresh";

  /**
   * Generate a short-lived access token (15 minutes).
   * Contains userId, email, and role for authorization.
   */
  public String generateAccessToken(UUID userId, String email, String role) {
    try {
      Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
      Date now = new Date();
      Date expiryDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRY_MS);

      return JWT.create()
          .withIssuer(ISSUER)
          .withSubject(userId.toString())
          .withClaim(CLAIM_EMAIL, email)
          .withClaim(CLAIM_ROLE, role != null ? role : "user")
          .withClaim(CLAIM_TOKEN_TYPE, TYPE_ACCESS)
          .withIssuedAt(now)
          .withExpiresAt(expiryDate)
          .sign(algorithm);

    } catch (JWTCreationException exception) {
      throw new RuntimeException("Failed to create access token", exception);
    }
  }

  /**
   * Generate a long-lived refresh token (30 days).
   * Contains only userId and a unique tokenId for revocation tracking.
   */
  public String generateRefreshToken(UUID userId) {
    try {
      Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
      Date now = new Date();
      Date expiryDate = new Date(now.getTime() + REFRESH_TOKEN_EXPIRY_MS);
      String tokenId = UUID.randomUUID().toString();

      return JWT.create()
          .withIssuer(ISSUER)
          .withSubject(userId.toString())
          .withClaim(CLAIM_TOKEN_TYPE, TYPE_REFRESH)
          .withClaim(CLAIM_TOKEN_ID, tokenId)
          .withIssuedAt(now)
          .withExpiresAt(expiryDate)
          .sign(algorithm);

    } catch (JWTCreationException exception) {
      throw new RuntimeException("Failed to create refresh token", exception);
    }
  }

  /**
   * Backward-compatible method — generates an access token.
   */
  public String generateToken(UUID userId, String email) {
    return generateAccessToken(userId, email, "user");
  }

  /**
   * Extract userId from any valid JWT (access or refresh).
   */
  public UUID getUserIdFromJWT(String token) {
    DecodedJWT decoded = decodeToken(token);
    return decoded != null ? UUID.fromString(decoded.getSubject()) : null;
  }

  /**
   * Extract userId ONLY from a valid access token.
   * Returns null if the token is a refresh token or invalid.
   */
  public UUID getUserIdFromAccessToken(String token) {
    DecodedJWT decoded = decodeToken(token);
    if (decoded == null) return null;

    String tokenType = decoded.getClaim(CLAIM_TOKEN_TYPE).asString();
    if (!TYPE_ACCESS.equals(tokenType)) return null;

    return UUID.fromString(decoded.getSubject());
  }

  /**
   * Extract the unique token ID from a refresh token (for revocation tracking).
   */
  public String getTokenIdFromRefreshToken(String token) {
    DecodedJWT decoded = decodeToken(token);
    if (decoded == null) return null;

    String tokenType = decoded.getClaim(CLAIM_TOKEN_TYPE).asString();
    if (!TYPE_REFRESH.equals(tokenType)) return null;

    return decoded.getClaim(CLAIM_TOKEN_ID).asString();
  }

  /**
   * Check if a token is a valid refresh token.
   */
  public boolean isValidRefreshToken(String token) {
    DecodedJWT decoded = decodeToken(token);
    if (decoded == null) return false;
    return TYPE_REFRESH.equals(decoded.getClaim(CLAIM_TOKEN_TYPE).asString());
  }

  /**
   * Validate any token.
   */
  public boolean validateToken(String token) {
    return decodeToken(token) != null;
  }

  /**
   * Get the refresh token expiry in seconds (for Redis TTL).
   */
  public long getRefreshTokenExpirySeconds() {
    return REFRESH_TOKEN_EXPIRY_MS / 1000;
  }

  private DecodedJWT decodeToken(String token) {
    try {
      Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
      JWTVerifier verifier = JWT.require(algorithm)
          .withIssuer(ISSUER)
          .build();
      return verifier.verify(token);
    } catch (JWTVerificationException exception) {
      return null;
    }
  }
}
