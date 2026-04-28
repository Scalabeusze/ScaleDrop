/*
 * Copyright 2026-present Scalabeusze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.scaledrop.sdiam.configuration.logging;

import com.scaledrop.sdiam.configuration.security.SessionAccountPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

  @Override
  protected boolean shouldNotFilterAsyncDispatch() {
    return false;
  }

  @Override
  protected boolean shouldNotFilterErrorDispatch() {
    return false;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    long startedAt = System.currentTimeMillis();

    putIfPresent("httpMethod", request.getMethod());
    putIfPresent("requestPath", request.getRequestURI());
    putIfPresent("remoteIp", request.getRemoteAddr());
    putIfPresent("remoteUser", request.getRemoteUser());

    try {
      filterChain.doFilter(request, response);
    } finally {
      addAuthenticatedContext(request);
      log.info(
          "status={} durationMs={}", response.getStatus(), System.currentTimeMillis() - startedAt);
      clearLoggingContext();
    }
  }

  private void addAuthenticatedContext(HttpServletRequest request) {
    Object principal = null;
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null) {
      principal = authentication.getPrincipal();
    } else if (request.getUserPrincipal() instanceof Authentication requestAuthentication) {
      principal = requestAuthentication.getPrincipal();
    }

    if (principal instanceof SessionAccountPrincipal sessionPrincipal) {
      putIfPresent("accountId", sessionPrincipal.getAccountId().toString());
      putIfPresent("username", sessionPrincipal.getUsername());
      putIfPresent("authProvider", sessionPrincipal.getProvider().name());
    }
  }

  private void putIfPresent(String key, String value) {
    if (value != null && !value.isBlank()) {
      MDC.put(key, value);
    }
  }

  private void clearLoggingContext() {
    MDC.remove("httpMethod");
    MDC.remove("requestPath");
    MDC.remove("remoteIp");
    MDC.remove("accountId");
    MDC.remove("username");
    MDC.remove("authProvider");
  }
}
