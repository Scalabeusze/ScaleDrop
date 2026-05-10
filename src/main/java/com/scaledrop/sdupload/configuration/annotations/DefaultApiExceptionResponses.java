package com.scaledrop.sdupload.configuration.annotations;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

import com.scaledrop.sdupload.configuration.Constants;
import com.scaledrop.sdupload.configuration.exception.api.ApiExceptionResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({PARAMETER, METHOD, FIELD, ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(
    responseCode = "400",
    description = Constants.INVALID_REQUEST_VALIDATION_FAILED,
    content = @Content(schema = @Schema(implementation = ApiExceptionResponse.class)))
@ApiResponse(
    responseCode = "401",
    description = Constants.INVALID_AUTHORIZATION,
    content = @Content(schema = @Schema(implementation = ApiExceptionResponse.class)))
@ApiResponse(
    responseCode = "404",
    description = Constants.NOT_FOUND,
    content = @Content(schema = @Schema(implementation = ApiExceptionResponse.class)))
public @interface DefaultApiExceptionResponses {}
