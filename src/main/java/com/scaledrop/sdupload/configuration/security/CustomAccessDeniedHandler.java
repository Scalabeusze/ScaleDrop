package com.scaledrop.sdupload.configuration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaledrop.sdupload.configuration.exception.api.ApiExceptionResponse;
import com.scaledrop.sdupload.configuration.exception.api.ApiExceptionType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

@Slf4j
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper mapper;

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws IOException {

    log.error(accessDeniedException.getMessage());

    var apiExceptionResponse = ApiExceptionResponse.builder()
        .message(accessDeniedException.getMessage())
        .type(ApiExceptionType.FORBIDDEN)
        .build();

    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setCharacterEncoding("UTF-8");
    response.getWriter().write(mapper.writeValueAsString(apiExceptionResponse));
    response.getWriter().flush();
  }

}
