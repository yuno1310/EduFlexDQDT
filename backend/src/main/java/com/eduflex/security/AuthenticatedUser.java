package com.eduflex.security;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

public final class AuthenticatedUser {
  private AuthenticatedUser() {
  }

  public static UUID requireOwner(Authentication authentication, UUID requestedUserId) {
    if (authentication == null || !(authentication.getPrincipal() instanceof UUID authenticatedUserId)) {
      throw new AccessDeniedException("An authenticated learner is required");
    }
    if (!authenticatedUserId.equals(requestedUserId)) {
      throw new AccessDeniedException("You cannot modify another learner's data");
    }
    return authenticatedUserId;
  }
}
