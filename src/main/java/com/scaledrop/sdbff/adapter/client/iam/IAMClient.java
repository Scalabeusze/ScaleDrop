package com.scaledrop.sdbff.adapter.client.iam;

import com.scaledrop.sdbff.adapter.api.model.iam.request.*;
import com.scaledrop.sdbff.adapter.api.model.iam.response.*;
import com.scaledrop.sdbff.adapter.client.iam.configuration.IAMClientConfiguration;
import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "iam-service-client",
    url = "${iam-service.url}",
    configuration = IAMClientConfiguration.class)
public interface IAMClient {

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
  AccountIAMResponse getAccount(@PathVariable("accountId") UUID accountId);

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
  void deleteAccount(@PathVariable("accountId") UUID accountId);
}
