package com.scaledrop.sdupload.configuration.exception.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApiExceptionType {
  UNAUTHORIZED(ExceptionTypeSeverity.WARN),
  FORBIDDEN(ExceptionTypeSeverity.WARN),
  VALIDATION(ExceptionTypeSeverity.WARN),
  NOT_FOUND(ExceptionTypeSeverity.WARN),
  CONFLICT(ExceptionTypeSeverity.WARN),
  UNPROCESSABLE_ENTITY(ExceptionTypeSeverity.WARN),
  EXTERNAL_SERVER_ERROR(ExceptionTypeSeverity.ERROR),
  INTERNAL_SERVER_ERROR(ExceptionTypeSeverity.ERROR);

  private final ExceptionTypeSeverity severity;

  public enum ExceptionTypeSeverity {
    ERROR, WARN
  }
}
