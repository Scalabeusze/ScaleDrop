package com.scaledrop.sdupload.configuration.exception.api;

import static com.scaledrop.sdupload.configuration.exception.api.ApiExceptionType.FORBIDDEN;
import static com.scaledrop.sdupload.configuration.exception.api.ApiExceptionType.INTERNAL_SERVER_ERROR;
import static com.scaledrop.sdupload.configuration.exception.api.ApiExceptionType.NOT_FOUND;
import static com.scaledrop.sdupload.configuration.exception.api.ApiExceptionType.VALIDATION;

import com.google.common.collect.ImmutableMap.Builder;
import com.scaledrop.sdupload.configuration.exception.SdUploadServiceException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Component
public class ApiExceptionResponseDetailsResolver {

  public static final String SERVER_ERROR = "Server error";
  public static final String VALIDATION_ERROR = "Validation error";

  private static final Map<Class<? extends Exception>, ApiExceptionResponseDetails> EXCEPTION_RESPONSE_DETAILS_MAP =
      new Builder<Class<? extends Exception>, ApiExceptionResponseDetails>()
          .put(HttpMessageNotReadableException.class, ApiExceptionResponseDetails.details("Not readable body", VALIDATION))
          .put(HttpRequestMethodNotSupportedException.class, ApiExceptionResponseDetails.details("HTTP method not allowed", FORBIDDEN))
          .put(MethodArgumentNotValidException.class, ApiExceptionResponseDetails.details(VALIDATION_ERROR, VALIDATION))
          .put(MissingServletRequestParameterException.class, ApiExceptionResponseDetails.details(VALIDATION_ERROR, VALIDATION))
          .put(ConstraintViolationException.class, ApiExceptionResponseDetails.details(VALIDATION_ERROR, VALIDATION))
          .put(IllegalArgumentException.class, ApiExceptionResponseDetails.details(VALIDATION))
          .put(MethodArgumentTypeMismatchException.class, ApiExceptionResponseDetails.details(VALIDATION))
          .put(EntityNotFoundException.class, ApiExceptionResponseDetails.details(NOT_FOUND))
          .put(JpaObjectRetrievalFailureException.class, ApiExceptionResponseDetails.details(NOT_FOUND))

          // Service specific exceptions
          .put(SdUploadServiceException.class, ApiExceptionResponseDetails.details("Internal server error", INTERNAL_SERVER_ERROR))
          .build();

  private List<ValidationError> resolveValidationErrors(Throwable ex) {
    if (ex instanceof BindException exception) {
      return exception
          .getBindingResult()
          .getAllErrors().stream()
          .filter(objectError -> StringUtils.isNotBlank(objectError.getDefaultMessage()))
          .map(objectError -> {
            if (objectError instanceof FieldError fieldError) {
              return new ValidationError(fieldError.getField(), fieldError.getDefaultMessage());
            }
            return new ValidationError(null, objectError.getDefaultMessage());
          })
          .sorted(Comparator.comparing(validationError -> {
            if (validationError.field() != null) {
              return validationError.field();
            }
            return validationError.error();
          }))
          .toList();
    }

    if (ex instanceof ConstraintViolationException exception) {
      return exception.getConstraintViolations().stream()
          .map(objectError ->
              new ValidationError(
                  objectError.getPropertyPath().toString(),
                  objectError.getMessage())
          ).toList();
    }
    return List.of();
  }

  public ApiExceptionResponse buildApiExceptionResponse(Exception ex) {
    ApiExceptionResponseDetails responseDetails = EXCEPTION_RESPONSE_DETAILS_MAP.getOrDefault(ex.getClass(), ApiExceptionResponseDetails.DEFAULT);
    String message = Optional.ofNullable(responseDetails.defaultMessage).orElse(ex.getMessage());
    List<ValidationError> validationErrors = resolveValidationErrors(ex);
    ApiExceptionType type = responseDetails.exceptionType;

    return ApiExceptionResponse.builder()
        .exception(ex)
        .type(type)
        .message(message)
        .errors(validationErrors)
        .build();
  }

  @AllArgsConstructor
  protected static class ApiExceptionResponseDetails {

    protected static final ApiExceptionResponseDetails DEFAULT = details(SERVER_ERROR, INTERNAL_SERVER_ERROR);
    protected Function<String, String> messageFunction;

    protected String defaultMessage;
    protected ApiExceptionType exceptionType;

    public ApiExceptionResponseDetails(ApiExceptionType exceptionType) {
      this.exceptionType = exceptionType;
    }

    static ApiExceptionResponseDetails details(String message, ApiExceptionType exceptionType) {
      return new ApiExceptionResponseDetails((String exceptionMessage) -> message, message, exceptionType);
    }

    static ApiExceptionResponseDetails details(ApiExceptionType exceptionType) {
      return new ApiExceptionResponseDetails(exceptionType);
    }

  }
}
