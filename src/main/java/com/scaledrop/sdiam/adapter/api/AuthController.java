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

package com.scaledrop.sdiam.adapter.api;

import static com.scaledrop.sdiam.configuration.Constants.API_V1_PREFIX;
import static com.scaledrop.sdiam.configuration.Constants.BASIC_AUTH;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.scaledrop.sdiam.adapter.api.model.request.LoginWithGoogleAPIRequest;
import com.scaledrop.sdiam.adapter.api.model.response.JwtAPIResponse;
import com.scaledrop.sdiam.application.service.AuthenticationService;
import com.scaledrop.sdiam.configuration.annotations.DefaultApiExceptionResponses;
import com.scaledrop.sdiam.configuration.security.JwtTokenService;
import com.scaledrop.sdiam.configuration.security.SessionAccountPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "Authentication controller", description = "API for user authentication")
public class AuthController {

  public static final String GOOGLE_LOGIN_ENDPOINT = API_V1_PREFIX + "/login/google";

  private final AuthenticationService authenticationService;
  private final JwtTokenService jwtTokenService;

  @PostMapping(
      value = GOOGLE_LOGIN_ENDPOINT,
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Log in with Google account",
      description =
          "Issues a signed JWT after successful Google authentication and optionally creates an"
              + " account")
  @SecurityRequirement(name = BASIC_AUTH)
  @DefaultApiExceptionResponses
  @ResponseStatus(HttpStatus.OK)
  public JwtAPIResponse loginWithGoogle(@Valid @RequestBody LoginWithGoogleAPIRequest request) {
    log.info("[AUTH] Received request to login with Google account");
    SessionAccountPrincipal sessionAccountPrincipal =
        authenticationService.authGoogleWithToken(request.getGoogleIdToken());
    return new JwtAPIResponse(jwtTokenService.createToken(sessionAccountPrincipal));
  }
}
