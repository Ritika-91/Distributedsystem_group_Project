//Reference:https://www.baeldung.com/spring-security-oauth-jwt
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
    if (path.startsWith("/auth")) return true;
    if (path.startsWith("/actuator")) return true;
    return false;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      chain.doFilter(request, response);
      return;
    }
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