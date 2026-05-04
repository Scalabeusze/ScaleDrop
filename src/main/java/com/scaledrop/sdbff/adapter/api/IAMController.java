package com.scaledrop.sdbff.adapter.api;

import static com.scaledrop.sdbff.configuration.Constants.API_V1_PREFIX;

import com.scaledrop.sdbff.adapter.api.model.iam.request.*;
import com.scaledrop.sdbff.adapter.api.model.iam.response.*;
import com.scaledrop.sdbff.application.port.in.iam.*;
import com.scaledrop.sdbff.configuration.annotations.DefaultApiExceptionResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "IAM", description = "Session and user accounts management")
public class IAMController {

  private final LoginUseCase authUseCase;
  private final GetAccountUseCase getAccountUseCase;
  private final CreateAccountUseCase createAccountUseCase;
  private final UpdateAccountUseCase updateAccountUseCase;
  private final UpdatePasswordUseCase updatePasswordUseCase;
  private final DeleteAccountUseCase deleteAccountUseCase;

  @PostMapping(value = API_V1_PREFIX + "/session/login")
  @Operation(
      summary = "Log in user",
      description = "Returns a JWT token upon providing valid credentials")
  @DefaultApiExceptionResponses
  @ResponseStatus(HttpStatus.OK)
  public JwtIAMResponse login(@RequestBody @Validated SessionLoginIAMRequest request) {
    return authUseCase.login(request);
  }

  @GetMapping(value = API_V1_PREFIX + "/accounts")
  @Operation(summary = "Get all accounts")
  @SecurityRequirement(name = "bearerAuth")
  @DefaultApiExceptionResponses
  @ResponseStatus(HttpStatus.OK)
  public List<AccountIAMResponse> getAccounts() {
    return getAccountUseCase.getAllAccounts();
  }

  @GetMapping(value = API_V1_PREFIX + "/accounts/{accountId}")
  @Operation(summary = "Get account by ID")
  @SecurityRequirement(name = "bearerAuth")
  @DefaultApiExceptionResponses
  @ResponseStatus(HttpStatus.OK)
  public AccountIAMResponse getAccount(@PathVariable UUID accountId) {
    return getAccountUseCase.getAccountById(accountId);
  }

  @PostMapping(value = API_V1_PREFIX + "/accounts")
  @Operation(summary = "Create a new account")
  @DefaultApiExceptionResponses
  @ResponseStatus(HttpStatus.CREATED)
  public AccountIAMResponse createAccount(@RequestBody @Validated CreateAccountIAMRequest request) {
    return createAccountUseCase.createAccount(request);
  }

  @PutMapping(value = API_V1_PREFIX + "/accounts/{accountId}")
  @Operation(summary = "Update account")
  @SecurityRequirement(name = "bearerAuth")
  @DefaultApiExceptionResponses
  @ResponseStatus(HttpStatus.OK)
  public AccountIAMResponse updateAccount(
      @PathVariable UUID accountId, @RequestBody @Validated UpdateAccountIAMRequest request) {
    return updateAccountUseCase.updateAccount(accountId, request);
  }

  @PatchMapping(value = API_V1_PREFIX + "/accounts/{accountId}/password")
  @Operation(summary = "Change password")
  @SecurityRequirement(name = "bearerAuth")
  @DefaultApiExceptionResponses
  @ResponseStatus(HttpStatus.OK)
  public AccountIAMResponse updatePassword(
      @PathVariable UUID accountId, @RequestBody @Validated UpdatePasswordIAMRequest request) {
    return updatePasswordUseCase.updatePassword(accountId, request);
  }

  @DeleteMapping(value = API_V1_PREFIX + "/accounts/{accountId}")
  @Operation(summary = "Delete account")
  @SecurityRequirement(name = "bearerAuth")
  @DefaultApiExceptionResponses
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteAccount(@PathVariable UUID accountId) {
    deleteAccountUseCase.deleteAccount(accountId);
  }
}
