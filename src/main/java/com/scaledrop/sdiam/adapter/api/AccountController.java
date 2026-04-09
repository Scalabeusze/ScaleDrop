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
import com.scaledrop.sdiam.adapter.api.model.request.CreateAccountAPIRequest;
import com.scaledrop.sdiam.adapter.api.model.request.UpdateAccountAPIRequest;
import com.scaledrop.sdiam.adapter.api.model.request.UpdatePasswordAPIRequest;
import com.scaledrop.sdiam.adapter.api.model.response.AccountAPIResponse;
import com.scaledrop.sdiam.application.service.AccountService;
import com.scaledrop.sdiam.configuration.annotations.DefaultApiExceptionResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Create account", description = "Creates a new account")
  @SecurityRequirement(name = BASIC_AUTH)
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "201", description = "Successfully created account")
  @ResponseStatus(HttpStatus.CREATED)
  public AccountAPIResponse createAccount(@Valid @RequestBody CreateAccountAPIRequest request) {
    log.info("[ACCOUNT] Received a request to create an account");
    return accountResponseMapper.toResponse(accountService.createAccount(request));
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "List accounts", description = "Fetches all accounts")
  @SecurityRequirement(name = BASIC_AUTH)
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "200", description = "Successfully fetched accounts")
  @ResponseStatus(HttpStatus.OK)
  public List<AccountAPIResponse> getAccounts() {
    log.info("[ACCOUNT] Received a request to fetch all accounts");
    return accountService.getAccounts().stream().map(accountResponseMapper::toResponse).toList();
  }

  @GetMapping(value = "/{accountId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get account", description = "Fetches an account by id")
  @SecurityRequirement(name = BASIC_AUTH)
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "200", description = "Successfully fetched account")
  @ResponseStatus(HttpStatus.OK)
  public AccountAPIResponse getAccount(@PathVariable UUID accountId) {
    log.info("[ACCOUNT] Received a request to fetch account {}", accountId);
    return accountResponseMapper.toResponse(accountService.getAccountById(accountId));
  }

  @PutMapping(
      value = "/{accountId}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Update account", description = "Updates non-password account fields")
  @SecurityRequirement(name = BASIC_AUTH)
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "200", description = "Successfully updated account")
  @ResponseStatus(HttpStatus.OK)
  public AccountAPIResponse updateAccount(
      @PathVariable UUID accountId, @Valid @RequestBody UpdateAccountAPIRequest request) {
    log.info("[ACCOUNT] Received a request to update account {}", accountId);
    return accountResponseMapper.toResponse(accountService.updateAccount(accountId, request));
  }

  @PatchMapping(
      value = "/{accountId}/password",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Update password", description = "Changes account password")
  @SecurityRequirement(name = BASIC_AUTH)
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "200", description = "Successfully updated password")
  @ResponseStatus(HttpStatus.OK)
  public AccountAPIResponse updatePassword(
      @PathVariable UUID accountId, @Valid @RequestBody UpdatePasswordAPIRequest request) {
    log.info("[ACCOUNT] Received a request to update password for account {}", accountId);
    return accountResponseMapper.toResponse(accountService.updatePassword(accountId, request));
  }

  @DeleteMapping("/{accountId}")
  @Operation(summary = "Delete account", description = "Deletes an account by id")
  @SecurityRequirement(name = BASIC_AUTH)
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "204", description = "Successfully deleted account")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteAccount(@PathVariable UUID accountId) {
    log.info("[ACCOUNT] Received a request to delete account {}", accountId);
    accountService.deleteAccount(accountId);
  }
}
