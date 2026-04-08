package com.scaledrop.sdupload.configuration.exception.api;

import com.scaledrop.sdupload.configuration.exception.api.ApiExceptionType.ExceptionTypeSeverity;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ApiExceptionResponse {

  @NotNull
  @Schema(
      requiredMode = RequiredMode.REQUIRED,
      example = "09abed2a-0a2b-48d5-b9e8-9da6ea309e44",
      description = "Error identification code")
  private UUID uuid;

  @NotNull
  @Schema(
      requiredMode = RequiredMode.REQUIRED,
      example = "Incorrect process status, expected: [X] received: Y",
      description = "String message error")
  private String message;

  @NotNull
  @Schema(
      requiredMode = RequiredMode.REQUIRED,
      example = "One of [UNAUTHORIZED, VALIDATION, NOT_FOUND, INTERNAL_SERVER_ERROR]",
      description = "Type of api exception error")
  private ApiExceptionType type;

  @NotNull
  @Schema(
      requiredMode = RequiredMode.REQUIRED,
      example = "2020-05-27T11:17:22.439Z",
      description = "Error occurrence timestamp")
  private OffsetDateTime timestamp;

  @Schema(
      description = "List of validation errors")
  private List<ValidationError> errors;

  @Builder
  public ApiExceptionResponse(
    @NonNull String message,
    @NonNull ApiExceptionType type,
    Exception exception,
    List<ValidationError> errors) {
    this.message = message;
    this.type = type;
    this.errors = errors;
    this.uuid = UUID.randomUUID();
    this.timestamp = OffsetDateTime.now();

    ExceptionTypeSeverity severity = type.getSeverity();
    switch (severity) {
      case WARN -> log.warn("{}", this, exception);
      case ERROR -> log.error("{}", this, exception);
    }
  }
}
