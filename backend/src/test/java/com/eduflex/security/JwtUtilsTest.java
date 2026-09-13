package com.eduflex.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtUtilsTest {

  @Test
  void signedTokenCarriesUserIdAndRole() {
    JwtUtils jwt = new JwtUtils();
    ReflectionTestUtils.setField(jwt, "jwtSecret", "a-test-secret-that-is-long-enough-for-hmac");
    ReflectionTestUtils.setField(jwt, "jwtExpirationMs", 60_000L);
    UUID userId = UUID.randomUUID();

    String token = jwt.generateToken(userId, "learner@example.com", "admin");

    assertEquals(userId, jwt.getUserIdFromJWT(token));
    assertEquals("admin", jwt.getRoleFromJWT(token));
  }
}
