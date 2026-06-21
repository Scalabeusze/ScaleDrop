package com.scaledrop.sdbff.adapter.client.iam;

import com.scaledrop.sdbff.adapter.api.model.account.response.AccountSearchAPIResponse;
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

  @GetMapping("/api/v1/accounts/search")
  List<AccountSearchAPIResponse> searchAccounts(
      @RequestParam("query") String query,
      @RequestParam(value = "limit", required = false) Integer limit);
}
