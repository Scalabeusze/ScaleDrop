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

import com.scaledrop.sdiam.adapter.api.model.request.SessionLoginAPIRequest;
import com.scaledrop.sdiam.adapter.api.model.response.JwtAPIResponse;
import com.scaledrop.sdiam.application.service.AuthenticationService;
import com.scaledrop.sdiam.configuration.annotations.DefaultApiExceptionResponses;
import com.scaledrop.sdiam.configuration.security.JwtTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(SessionController.SESSION_ENDPOINT)
@Tag(name = "Session", description = "JWT login controller")
public class SessionController {

  static final String GOOGLE_REGISTRATION_ID = "/google";
  static final String SESSION_ENDPOINT = API_V1_PREFIX + "/session";

  private final AuthenticationService authService;
  private final JwtTokenService jwtTokenService;

  @PostMapping(
      value = "/login",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Log in with local account", description = "Issues a signed JWT")
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "200", description = "Successfully issued JWT")
  @ResponseStatus(HttpStatus.OK)
  public JwtAPIResponse login(@Valid @RequestBody SessionLoginAPIRequest request) {
    var principal = authService.authLocal(request.username(), request.password());
    return new JwtAPIResponse(jwtTokenService.createToken(principal));
  }

  @GetMapping(GOOGLE_REGISTRATION_ID)
  @Operation(
      summary = "Start Google login",
      description =
          "Redirects the browser to the Spring Security Google OAuth entrypoint. The OAuth callback"
              + " is handled by Spring Security and is not exposed as a controller endpoint.")
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "302", description = "Redirects to Google OAuth login")
  @ResponseStatus(HttpStatus.FOUND)
  public RedirectView loginWithGoogle(HttpServletRequest request) {
    return new RedirectView(
        request.getContextPath()
            + OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI
            + GOOGLE_REGISTRATION_ID);
  }
}
