package com.scaledrop.sdbff.adapter.api;

import static com.scaledrop.sdbff.configuration.Constants.API_V1_PREFIX;

import com.scaledrop.sdbff.adapter.api.mapper.AccountResponseMapper;
import com.scaledrop.sdbff.adapter.api.model.response.AccountAPIResponse;
import com.scaledrop.sdbff.application.port.in.AccountUseCase;
import com.scaledrop.sdbff.configuration.annotations.DefaultApiExceptionResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "Account", description = "Account controller")
public class AccountController {

  private static final String ACCOUNT_ENDPOINT = API_V1_PREFIX + "/account";

  private final AccountUseCase accountUseCase;
  private final AccountResponseMapper accountResponseMapper;

  @GetMapping(value = ACCOUNT_ENDPOINT + "/me", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get account details", description = "Extracts user ID from JWT token")
  @SecurityRequirement(name = "bearerAuth")
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "200", description = "Successfully fetched account details")
  @ResponseStatus(HttpStatus.OK)
  public AccountAPIResponse getMyAccount(@AuthenticationPrincipal Jwt jwt) {
    String subject = jwt.getSubject();
    UUID accountId = UUID.fromString(subject);
    log.info("[ACCOUNT-CONTROLLER] Token verified. Fetching data for user: {}", accountId);
    return accountResponseMapper.toResponse(accountUseCase.getAccountObject(accountId));
  }
}
