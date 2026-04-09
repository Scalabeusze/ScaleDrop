package com.scaledrop.sdbff.adapter.api;

import static com.scaledrop.sdbff.configuration.Constants.API_V1_PREFIX;
import static com.scaledrop.sdbff.configuration.Constants.BASIC_AUTH;

import com.scaledrop.sdbff.adapter.api.mapper.ExampleResponseMapper;
import com.scaledrop.sdbff.adapter.api.model.response.ExampleAPIResponse;
import com.scaledrop.sdbff.application.port.in.ExampleUseCase;
import com.scaledrop.sdbff.configuration.annotations.DefaultApiExceptionResponses;
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
