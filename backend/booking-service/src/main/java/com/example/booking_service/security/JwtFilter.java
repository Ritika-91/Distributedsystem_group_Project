package com.example.booking_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtFilter extends OncePerRequestFilter {

  private final JwtAuth jwtAuth;

  public JwtFilter(JwtAuth jwtAuth) {
    this.jwtAuth = jwtAuth;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();

    // ✅ allow auth endpoints (if booking-service has any auth passthrough)
    if (path.startsWith("/auth")) return true;

    // ✅ allow actuator if present
    if (path.startsWith("/actuator")) return true;

    return false;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    // ✅ allow preflight always - THIS MUST BE IN doFilterInternal, NOT shouldNotFilter
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      chain.doFilter(request, response);
      return;
    }

    // ✅ If you want to enforce JWT on booking endpoints:
    // just parse it and attach userId somewhere if needed, or let controller read header.
    // Most important: DO NOT block OPTIONS.
    try {
      String auth = request.getHeader("Authorization");
      if (auth != null && auth.startsWith("Bearer ")) {
        jwtAuth.requirePrincipal(auth); // validates token
      }
      chain.doFilter(request, response);
    } catch (Exception e) {
      response.setStatus(401);
      response.getWriter().write("Unauthorized: " + e.getMessage());
    }
  }
}