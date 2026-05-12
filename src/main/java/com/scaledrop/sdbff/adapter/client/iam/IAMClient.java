package com.scaledrop.sdbff.adapter.client.iam;

import com.scaledrop.sdbff.adapter.api.model.iam.request.*;
import com.scaledrop.sdbff.adapter.api.model.iam.response.*;
import com.scaledrop.sdbff.adapter.client.iam.configuration.IAMClientConfiguration;
import com.scaledrop.sdbff.adapter.client.iam.model.request.IAMLoginRequest;
import com.scaledrop.sdbff.adapter.client.iam.model.request.IAMUpdateAccountRequest;
import com.scaledrop.sdbff.adapter.client.iam.model.response.IAMAccountResponse;
import com.scaledrop.sdbff.adapter.client.iam.model.response.IAMJWTResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "iam-service-client",
    url = "${iam-service.url}",
    configuration = IAMClientConfiguration.class)
public interface IAMClient {

  // --- LOGIN ---
  @PostMapping(
      value = "/api/v1/login/google",
      produces = "application/json",
      consumes = "application/json")
  IAMJWTResponse login(IAMLoginRequest request);

  // --- ACCOUNTS ---
  @GetMapping(value = "/api/v1/accounts/{accountId}", produces = "application/json")
  IAMAccountResponse getAccount(@PathVariable UUID accountId);

  @GetMapping(value = "/api/v1/accounts", produces = "application/json")
  IAMAccountResponse getAccountByUsername(@RequestParam(value = "username") String username);

  @PutMapping(
      value = "/api/v1/accounts/{accountId}",
      produces = "application/json",
      consumes = "application/json")
  IAMAccountResponse updateAccount(
      @PathVariable UUID accountId, @RequestBody IAMUpdateAccountRequest request);

  @DeleteMapping("/api/v1/accounts/{accountId}")
  void deleteAccount(@PathVariable UUID accountId);

  // --- SESJA ---
  @PostMapping(
      value = "/api/v1/session/login",
      produces = "application/json",
      consumes = "application/json")
  JwtIAMResponse login(@RequestBody SessionLoginIAMRequest request);

  // --- KONTA ---
  @GetMapping(value = "/api/v1/accounts", produces = "application/json")
  List<AccountIAMResponse> getAccounts();

  @GetMapping(value = "/api/v1/accounts/{accountId}", produces = "application/json")
  AccountIAMResponse getAccountOld(@PathVariable("accountId") UUID accountId);

  @PostMapping(
      value = "/api/v1/accounts",
      produces = "application/json",
      consumes = "application/json")
  AccountIAMResponse createAccount(@RequestBody CreateAccountIAMRequest request);

  @PutMapping(
      value = "/api/v1/accounts/{accountId}",
      produces = "application/json",
      consumes = "application/json")
  AccountIAMResponse updateAccount(
      @PathVariable("accountId") UUID accountId, @RequestBody UpdateAccountIAMRequest request);

  @PatchMapping(
      value = "/api/v1/accounts/{accountId}/password",
      produces = "application/json",
      consumes = "application/json")
  AccountIAMResponse updatePassword(
      @PathVariable("accountId") UUID accountId, @RequestBody UpdatePasswordIAMRequest request);

  @DeleteMapping(value = "/api/v1/accounts/{accountId}")
  void deleteAccountOld(@PathVariable("accountId") UUID accountId);
}
