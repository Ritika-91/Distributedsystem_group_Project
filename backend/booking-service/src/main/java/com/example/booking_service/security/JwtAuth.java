
// Referenced from : https://www.baeldung.com/spring-security-oauth-jwt
package com.example.booking_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class JwtAuth {

 @Value("${JWT_SECRET_KEY:${jwt.secret}}")
  private String secret;

  private Claims claims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  public record UserPrincipal(Long userId, String username, String role) {}

  public UserPrincipal requirePrincipal(String authorization) {
    if (authorization == null || !authorization.startsWith("Bearer ")) {
        throw new RuntimeException("Missing or invalid Authorization header");
    }

    String token = authorization.substring("Bearer ".length());
    Claims c = claims(token);

    String username = c.getSubject();

    Object uidObj = c.get("user_id");
    Long userId = null;
    if (uidObj instanceof Number n) userId = n.longValue();
    else if (uidObj != null) userId = Long.valueOf(uidObj.toString());

    String role = c.get("role", String.class);

    if (username == null || username.isBlank()) {
        throw new RuntimeException("Token missing subject (sub)");
    }
    if (userId == null) {
        throw new RuntimeException("Token missing user_id");
    }

    return new UserPrincipal(userId, username, role);
}
  public Long requireUserId(String authorization) {
    return requirePrincipal(authorization).userId();
  }
}
