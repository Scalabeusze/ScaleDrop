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

import com.scaledrop.sdiam.adapter.api.mapper.AccountResponseMapper;
import com.scaledrop.sdiam.adapter.api.model.request.UpdateAccountAPIRequest;
import com.scaledrop.sdiam.adapter.api.model.response.AccountAPIResponse;
import com.scaledrop.sdiam.adapter.api.model.response.AccountSearchAPIResponse;
import com.scaledrop.sdiam.application.service.AccountService;
import com.scaledrop.sdiam.configuration.annotations.DefaultApiExceptionResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(AccountController.ACCOUNTS_ENDPOINT)
@Tag(name = "Accounts", description = "Account management controller")
public class AccountController {

  static final String ACCOUNTS_ENDPOINT = API_V1_PREFIX + "/accounts";

  private final AccountService accountService;
  private final AccountResponseMapper accountResponseMapper;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get account by username", description = "Fetches account by username")
  @SecurityRequirement(name = BASIC_AUTH)
  @DefaultApiExceptionResponses
  public AccountAPIResponse getByUsername(@RequestParam(value = "username") String username) {
    log.info("[ACCOUNT] Received request to get account by username: {}", username);
    return accountResponseMapper.toResponse(accountService.getAccountByUsername(username));
  }

  @GetMapping(value = "/{accountId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get account by ID", description = "Fetches an account by its unique UUID")
  @SecurityRequirement(name = BASIC_AUTH)
  @DefaultApiExceptionResponses
  public AccountAPIResponse getAccount(@PathVariable UUID accountId) {
    log.info("[ACCOUNT] Received request to get account by ID: {}", accountId);
    return accountResponseMapper.toResponse(accountService.getAccountById(accountId));
  }

  @PutMapping(
      value = "/{accountId}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Update account",
      description = "Updates profile fields (firstName, lastName, avatarUrl)")
  @SecurityRequirement(name = BASIC_AUTH)
  @DefaultApiExceptionResponses
  public AccountAPIResponse updateAccount(
      @PathVariable UUID accountId, @Valid @RequestBody UpdateAccountAPIRequest request) {
    log.info("[ACCOUNT] Received request to update: {}", accountId);
    return accountResponseMapper.toResponse(accountService.updateAccount(accountId, request));
  }

  @DeleteMapping("/{accountId}")
  @Operation(
      summary = "Deactivate account",
      description = "Logically deactivates an account by setting status to DISABLED")
  @SecurityRequirement(name = BASIC_AUTH)
  @DefaultApiExceptionResponses
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteAccount(@PathVariable UUID accountId) {
    log.info("[ACCOUNT] Received request to deactivate account: {}", accountId);
    accountService.deactivateAccount(accountId);
  }

  @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Search accounts", description = "Autocomplete for user search")
  @SecurityRequirement(name = BASIC_AUTH)
  @DefaultApiExceptionResponses
  public List<AccountSearchAPIResponse> searchAccounts(
      @RequestParam("query") @NotBlank @Size(min = 2, max = 100) String query,
      @RequestParam(value = "limit", required = false) Integer limit) {
    log.info("[ACCOUNT] Received request to search accounts: {}", query);
    return accountService.searchAccounts(query, limit).stream()
        .map(accountResponseMapper::toSearchResponse)
        .toList();
  }
}
