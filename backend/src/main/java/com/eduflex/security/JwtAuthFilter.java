package com.eduflex.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  @Autowired
  private JwtUtils jwtUtils;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String token = extractToken(request);

    if (StringUtils.hasText(token)) {
      UUID userId = jwtUtils.getUserIdFromJWT(token);
      if (userId != null) {
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    }

    filterChain.doFilter(request, response);
  }

  private String extractToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
      return header.substring(BEARER_PREFIX.length());
    }
    return null;
  }
}
