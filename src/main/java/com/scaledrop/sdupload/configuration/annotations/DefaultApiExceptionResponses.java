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
