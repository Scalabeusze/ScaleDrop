package com.scaledrop.sdbff.adapter.api;

import static com.scaledrop.sdbff.configuration.Constants.API_V1_PREFIX;
import static com.scaledrop.sdbff.configuration.Constants.BEARER_JWT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.scaledrop.sdbff.adapter.api.mapper.AccountMapper;
import com.scaledrop.sdbff.adapter.api.model.account.request.LoginAPIRequest;
import com.scaledrop.sdbff.adapter.api.model.account.request.UpdateAccountAPIRequest;
import com.scaledrop.sdbff.adapter.api.model.account.response.AccountAPIResponse;
import com.scaledrop.sdbff.adapter.api.model.iam.response.JwtAPIResponse;
import com.scaledrop.sdbff.application.component.UserContext;
import com.scaledrop.sdbff.application.port.in.IAMUseCase;
import com.scaledrop.sdbff.configuration.annotations.DefaultApiExceptionResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Account management controller")
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
      description =
          "Returns a signed JWT after successful Google authentication and optionally creates an"
              + " account")
  @DefaultApiExceptionResponses
  @ResponseStatus(HttpStatus.OK)
  public JwtAPIResponse login(@Valid LoginAPIRequest request) {
    log.info("[ACCOUNT] Received login request with Google ID token");
    return accountMapper.toJwtResponse(iamUseCase.login(request.getGoogleIdToken()));
  }

  @GetMapping(value = API_V1_PREFIX + "/account", produces = APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Get account information",
      description = "Returns account information for the currently authenticated user")
  @DefaultApiExceptionResponses
  @SecurityRequirement(name = BEARER_JWT)
  @ResponseStatus(HttpStatus.OK)
  public AccountAPIResponse getAccount() {
    UUID userId = userContext.getUserId();
    log.info("[ACCOUNT] Received fetch account request for user: {}", userId);
    return accountMapper.toAccountResponse(iamUseCase.getAccountById(userId));
  }

  @PutMapping(
      value = API_V1_PREFIX + "/account",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Update account information",
      description = "Updates account information for the currently authenticated user")
  @DefaultApiExceptionResponses
  @SecurityRequirement(name = BEARER_JWT)
  @ResponseStatus(HttpStatus.OK)
  public AccountAPIResponse updateAccount(@Valid @RequestBody UpdateAccountAPIRequest request) {
    UUID userId = userContext.getUserId();
    log.info("[ACCOUNT] Received update account request for user: {}", userId);
    return accountMapper.toAccountResponse(iamUseCase.updateAccount(userId, request));
  }

  @DeleteMapping(API_V1_PREFIX + "/account")
  @Operation(
      summary = "Deactivate account",
      description = "Deactivates the account of the currently authenticated user")
  @DefaultApiExceptionResponses
  @SecurityRequirement(name = BEARER_JWT)
  @ResponseStatus(HttpStatus.OK)
  public void deactivateAccount() {
    UUID userId = userContext.getUserId();
    log.info("[ACCOUNT] Received deactivate account request for user: {}", userId);
    iamUseCase.deactivateAccount(userId);
    log.info("[ACCOUNT] Succesfully deactivated account: {}", userId);
  }
}
