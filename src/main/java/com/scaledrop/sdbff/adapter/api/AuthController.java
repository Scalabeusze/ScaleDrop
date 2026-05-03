package com.scaledrop.sdbff.adapter.api;

import static com.scaledrop.sdbff.configuration.Constants.API_V1_PREFIX;

import com.scaledrop.sdbff.adapter.api.mapper.AuthResponseMapper;
import com.scaledrop.sdbff.adapter.api.model.request.LoginAPIRequest;
import com.scaledrop.sdbff.adapter.api.model.response.LoginAPIResponse;
import com.scaledrop.sdbff.application.port.in.AuthUseCase;
import com.scaledrop.sdbff.configuration.annotations.DefaultApiExceptionResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication controller")
public class AuthController {

  private static final String AUTH_ENDPOINT = API_V1_PREFIX + "/auth";

  private final AuthUseCase authUseCase;
  private final AuthResponseMapper authResponseMapper;

  @PostMapping(value = AUTH_ENDPOINT + "/login", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Login via Google Code",
      description = "Exchanges Google Authorization Code for JWT Access and Refresh tokens")
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "200", description = "Successfully logged in and generated tokens")
  @ResponseStatus(HttpStatus.OK)
  public LoginAPIResponse login(@Valid @RequestBody LoginAPIRequest request) {
    log.info("[AUTH-CONTROLLER] Received login request for Google code exchange");
    return authResponseMapper.toResponse(authUseCase.login(request.getGcode()));
  }
}
