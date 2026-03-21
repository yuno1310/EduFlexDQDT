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

  @Value("${JwtExpirationMs}")
  private long jwtExpirationMs;

  private final String ISSUER = "EduFlexApp";

  public String generateToken(UUID userId, String email) {
    try {
      Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
      Date now = new Date();
      Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
      return JWT.create()
          .withIssuer(ISSUER)
          .withSubject(userId.toString())
          .withClaim("email", email)
          .withIssuedAt(now)
          .withExpiresAt(expiryDate)
          .sign(algorithm);

    } catch (JWTCreationException exception) {
      throw new RuntimeException("Failed to create JWT Token", exception);
    }
  }

  public UUID getUserIdFromJWT(String token) {
    try {
      Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
      JWTVerifier verifier = JWT.require(algorithm)
          .withIssuer(ISSUER)
          .build();

      DecodedJWT decodedJWT = verifier.verify(token);

      return UUID.fromString(decodedJWT.getSubject());

    } catch (JWTVerificationException exception) {
      System.out.println("Token is not true or expired" + exception.getMessage());
      return null;
    }
  }

  public boolean validateToken(String token) {
    return getUserIdFromJWT(token) != null;
  }
}
