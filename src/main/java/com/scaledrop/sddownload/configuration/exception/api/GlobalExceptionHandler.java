package com.scaledrop.sddownload.configuration.exception.api;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Strings;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  private final ApiExceptionResponseDetailsResolver apiExceptionResponseDetailsResolver;

  @Order(HIGHEST_PRECEDENCE)
  @ResponseStatus(BAD_REQUEST)
  @ExceptionHandler({
      ConstraintViolationException.class,
      MethodArgumentNotValidException.class,
      HttpMessageNotReadableException.class,
      MissingServletRequestParameterException.class
  })
  public ApiExceptionResponse handleBadRequest(Exception ex) {
    return buildApiExceptionResponse(ex);
  }

  @Order(HIGHEST_PRECEDENCE)
  @ResponseStatus(NOT_FOUND)
  @ExceptionHandler({
      EntityNotFoundException.class,
      JpaObjectRetrievalFailureException.class,
  })
  public ApiExceptionResponse handleNotFound(Exception ex) {
    return buildApiExceptionResponse(ex);
  }

  @ResponseStatus(UNAUTHORIZED)
  @ExceptionHandler(AccessDeniedException.class)
  public ApiExceptionResponse handleUnauthorizedException(Exception ex) {
    return buildApiExceptionResponse(ex);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(BAD_REQUEST)
  public ApiExceptionResponse handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    return buildApiExceptionResponse(ex);
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(Exception.class)
  public ApiExceptionResponse handleGenericException(Exception ex) {
    return buildApiExceptionResponse(ex);
  }

  @ResponseStatus(METHOD_NOT_ALLOWED)
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ApiExceptionResponse handleMethodNotAllowedException(Exception ex) {
    return buildApiExceptionResponse(ex);
  }

  @ResponseStatus(SERVICE_UNAVAILABLE)
  @ExceptionHandler(IOException.class)
  public Object exceptionHandler(IOException ex) {
    if (checkForBrokenPipe(ex)) {
      return null;
    }
    return ApiExceptionResponse.builder()
        .type(ApiExceptionType.INTERNAL_SERVER_ERROR)
        .message(ex.getMessage())
        .build();
  }

  private ApiExceptionResponse buildApiExceptionResponse(Exception ex) {
    return apiExceptionResponseDetailsResolver.buildApiExceptionResponse(ex);
  }

  private boolean checkForBrokenPipe(IOException ex) {
    return Strings.CI.contains(getRootCauseMessage(ex), "Broken pipe");
  }

}
