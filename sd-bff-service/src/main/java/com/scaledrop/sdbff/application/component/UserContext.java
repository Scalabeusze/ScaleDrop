package com.scaledrop.sdbff.application.component;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class UserContext {

  private UUID userId;

  public UUID getUserId() {
    if (userId == null) {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth instanceof JwtAuthenticationToken jwt) {
        this.userId = UUID.fromString(jwt.getToken().getSubject());
      }
    }
    return userId;
  }
}
