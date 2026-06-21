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

package com.scaledrop.sddownload.configuration.security;

import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaledrop.sddownload.configuration.exception.api.ApiExceptionResponse;
import com.scaledrop.sddownload.configuration.exception.api.ApiExceptionType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomBasicAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {

  public static final String REALM_NAME = "CK_EV_REALM";
  private static final String BASIC_REALM = "Basic realm=";

  private final ObjectMapper objectMapper;

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {

    ApiExceptionResponse apiExceptionResponse = buildUnauthorizedResponse(authException);
    String unauthorizedResponse = objectMapper.writeValueAsString(apiExceptionResponse);
    response.getWriter().write(unauthorizedResponse);

    response.addHeader(WWW_AUTHENTICATE, BASIC_REALM + getRealmName());
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(APPLICATION_JSON_VALUE);
  }

  @Override
  public void afterPropertiesSet() {
    setRealmName(REALM_NAME);
    super.afterPropertiesSet();
  }

  private ApiExceptionResponse buildUnauthorizedResponse(AuthenticationException exception) {
    return ApiExceptionResponse.builder()
        .exception(exception)
        .message(exception.getMessage())
        .type(ApiExceptionType.UNAUTHORIZED)
        .build();
  }
}
