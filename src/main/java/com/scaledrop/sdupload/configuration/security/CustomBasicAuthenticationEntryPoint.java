package com.scaledrop.sdupload.configuration.security;

import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaledrop.sdupload.configuration.exception.api.ApiExceptionResponse;
import com.scaledrop.sdupload.configuration.exception.api.ApiExceptionType;
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
