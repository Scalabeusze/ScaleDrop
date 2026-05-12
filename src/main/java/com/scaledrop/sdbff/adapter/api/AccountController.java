package com.scaledrop.sdbff.adapter.api;

import static com.scaledrop.sdbff.configuration.Constants.API_V1_PREFIX;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.scaledrop.sdbff.adapter.api.mapper.AccountMapper;
import com.scaledrop.sdbff.adapter.api.model.iam.request.LoginAPIRequest;
import com.scaledrop.sdbff.adapter.api.model.iam.response.JwtAPIResponse;
import com.scaledrop.sdbff.application.component.UserContext;
import com.scaledrop.sdbff.application.port.in.IAMUseCase;
import com.scaledrop.sdbff.configuration.annotations.DefaultApiExceptionResponses;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class AccountController {

  private final IAMUseCase iamUseCase;
  private final UserContext userContext;
  private final AccountMapper accountMapper;

  @PostMapping(
      value = API_V1_PREFIX + "/login",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Log in with Google token",
      description = "Returns a signed JWT after successful Google authentication and optionally creates an account")
  @DefaultApiExceptionResponses
  @ResponseStatus(HttpStatus.OK)
  public JwtAPIResponse login(@Valid LoginAPIRequest request) {
    log.info("[ACCOUNT] Received login request with Google ID token");
    return accountMapper.toJwtResponse(iamUseCase.login(request.getGoogleIdToken()));
  }
}
