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

package com.scaledrop.sddownload.adapter.api;

import static com.scaledrop.sddownload.configuration.Constants.API_V1_PREFIX;
import static com.scaledrop.sddownload.configuration.Constants.BASIC_AUTH;

import com.scaledrop.sddownload.adapter.api.mapper.ExampleResponseMapper;
import com.scaledrop.sddownload.adapter.api.model.response.ExampleAPIResponse;
import com.scaledrop.sddownload.application.port.in.ExampleUseCase;
import com.scaledrop.sddownload.configuration.annotations.DefaultApiExceptionResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "Example", description = "Example controller")
public class ExampleController {

  private static final String EXAMPLE_ENDPOINT = API_V1_PREFIX + "/example";

  private final ExampleUseCase exampleUseCase;
  private final ExampleResponseMapper exampleResponseMapper;

  @GetMapping(value = EXAMPLE_ENDPOINT, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Fetch example object", description = "Just an example")
  @SecurityRequirement(name = BASIC_AUTH)
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "200", description = "Successfully fetched example object")
  @ResponseStatus(HttpStatus.OK)
  public ExampleAPIResponse getExample() {
    log.info("[EXAMPLE] Received a request");
    return exampleResponseMapper.toResponse(exampleUseCase.getExampleObject());
  }
}
