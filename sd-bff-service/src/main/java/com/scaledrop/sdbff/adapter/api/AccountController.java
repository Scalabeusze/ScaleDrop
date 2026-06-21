package com.scaledrop.sdbff.adapter.api;

import static com.scaledrop.sdbff.configuration.Constants.API_V1_PREFIX;
import static com.scaledrop.sdbff.configuration.Constants.BEARER_JWT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.scaledrop.sdbff.adapter.api.mapper.AccountMapper;
import com.scaledrop.sdbff.adapter.api.model.account.request.LoginAPIRequest;
import com.scaledrop.sdbff.adapter.api.model.account.request.UpdateAccountAPIRequest;
import com.scaledrop.sdbff.adapter.api.model.account.response.AccountAPIResponse;
import com.scaledrop.sdbff.adapter.api.model.account.response.AccountSearchAPIResponse;
import com.scaledrop.sdbff.adapter.api.model.account.response.JwtAPIResponse;
import com.scaledrop.sdbff.application.component.UserContext;
import com.scaledrop.sdbff.application.port.in.IAMUseCase;
import com.scaledrop.sdbff.configuration.annotations.DefaultApiExceptionResponses;
import com.scaledrop.sdbff.configuration.annotations.DefaultApiSecurity;
import com.scaledrop.sdbff.configuration.ratelimit.UserRateLimit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
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

  @UserRateLimit(capacity = 5, refillTokens = 5, refillMinutes = 1)
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
  public JwtAPIResponse login(@Valid @RequestBody LoginAPIRequest request) {
    log.info("[BFF-CONTROLLER] Received login request with Google ID token");
    return accountMapper.toJwtResponse(iamUseCase.login(request.getGoogleIdToken()));
  }

  @UserRateLimit
  @GetMapping(value = API_V1_PREFIX + "/account", produces = APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Get account information",
      description = "Returns account information for the currently authenticated user")
  @DefaultApiExceptionResponses
  @SecurityRequirement(name = BEARER_JWT)
  @ResponseStatus(HttpStatus.OK)
  public AccountAPIResponse getAccount() {
    UUID userId = userContext.getUserId();
    log.info("[BFF-CONTROLLER] Received fetch account request for user: {}", userId);
    return accountMapper.toAccountResponse(iamUseCase.getAccountById(userId));
  }

  @UserRateLimit(capacity = 10, refillTokens = 10, refillMinutes = 1)
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
    log.info("[BFF-CONTROLLER] Received update account request for user: {}", userId);
    return accountMapper.toAccountResponse(iamUseCase.updateAccount(userId, request));
  }

  @UserRateLimit(capacity = 2, refillTokens = 2, refillMinutes = 1)
  @DeleteMapping(API_V1_PREFIX + "/account")
  @Operation(
      summary = "Deactivate account",
      description = "Deactivates the account of the currently authenticated user")
  @DefaultApiExceptionResponses
  @SecurityRequirement(name = BEARER_JWT)
  @ResponseStatus(HttpStatus.OK)
  public void deactivateAccount() {
    UUID userId = userContext.getUserId();
    log.info("[BFF-CONTROLLER] Received deactivate account request for user: {}", userId);
    iamUseCase.deactivateAccount(userId);
    log.info("[BFF-CONTROLLER] Successfully deactivated account: {}", userId);
  }

  @UserRateLimit(capacity = 50, refillTokens = 50, refillMinutes = 1)
  @GetMapping("/search")
  @Operation(
      summary = "Search accounts",
      description = "Autocomplete endpoint for finding users by username or email snippet.")
  @DefaultApiSecurity
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "200", description = "Successfully found matching accounts")
  @ResponseStatus(HttpStatus.OK)
  public List<AccountSearchAPIResponse> searchAccounts(
      @RequestParam("query") @Size(min = 2, max = 100) String query,
      @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {

    log.info(
        "[BFF-CONTROLLER] Received account search request for query: '{}', limit: {}",
        query,
        limit);

    return iamUseCase.searchAccounts(query, limit).stream().map(accountMapper::toResponse).toList();
  }
}
