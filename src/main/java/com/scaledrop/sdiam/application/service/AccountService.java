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

package com.scaledrop.sdiam.application.service;

import com.scaledrop.sdiam.adapter.api.model.request.UpdateAccountAPIRequest;
import com.scaledrop.sdiam.adapter.db.AccountEntity;
import com.scaledrop.sdiam.adapter.db.AccountEntity.AccountStatus;
import com.scaledrop.sdiam.adapter.db.AccountRepository;
import com.scaledrop.sdiam.configuration.exception.AccountNotFoundException;
import com.scaledrop.sdiam.configuration.exception.AccountValidationException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

  private static final String ACCOUNT_NOT_FOUND = "Account not found";
  private static final String SEARCH_QUERY_REQUIRED = "Search query is required";
  private static final String SEARCH_QUERY_TOO_SHORT = "Search query must be at least 2 characters";
  private static final String SEARCH_QUERY_TOO_LONG = "Search query must be at most 100 characters";
  private static final int DEFAULT_SEARCH_LIMIT = 10;
  private static final int MAX_SEARCH_LIMIT = 20;
  private static final int MIN_SEARCH_QUERY_LENGTH = 2;
  private static final int MAX_SEARCH_QUERY_LENGTH = 100;

  private final AccountRepository accountRepository;

  // Read

  /**
   * Gets an account by primary key
   *
   * @param accountId UUID of the account
   * @return matching AccountEntity
   * @throws AccountNotFoundException if accountId does not exist
   */
  @Transactional(readOnly = true)
  public AccountEntity getAccountById(UUID accountId) {
    return accountRepository
        .findById(accountId)
        .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_NOT_FOUND));
  }

  /**
   * Gets an account by username
   *
   * @param username username of the account
   * @return matching AccountEntity
   * @throws AccountNotFoundException if username does not exist
   */
  @Transactional(readOnly = true)
  public AccountEntity getAccountByUsername(String username) {
    return accountRepository
        .findByUsernameAndStatusNot(username, AccountStatus.DISABLED)
        .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_NOT_FOUND));
  }

  /**
   * Searches active accounts for share-recipient autocomplete.
   *
   * @param query username fragment
   * @param limit maximum number of accounts to return
   * @return matching active accounts sorted by username
   */
  @Transactional(readOnly = true)
  public List<AccountEntity> searchAccounts(String query, Integer limit) {
    String normalizedQuery = StringUtils.trimToEmpty(query);
    if (StringUtils.isBlank(normalizedQuery)) {
      throw new AccountValidationException(SEARCH_QUERY_REQUIRED);
    }
    if (normalizedQuery.length() < MIN_SEARCH_QUERY_LENGTH) {
      throw new AccountValidationException(SEARCH_QUERY_TOO_SHORT);
    }
    if (normalizedQuery.length() > MAX_SEARCH_QUERY_LENGTH) {
      throw new AccountValidationException(SEARCH_QUERY_TOO_LONG);
    }

    return accountRepository.findByStatusAndUsernameContainingIgnoreCaseOrderByUsernameAsc(
        AccountStatus.ACTIVE, normalizedQuery, PageRequest.of(0, resolveSearchLimit(limit)));
  }

  // Update
  @Transactional
  public AccountEntity updateAccount(UUID accountId, UpdateAccountAPIRequest request) {
    var accountEntity = getAccountById(accountId);
    accountEntity.apply(request);
    return accountRepository.save(accountEntity);
  }

  // Delete

  /**
   * Deactivates and account
   *
   * @param accountId UUID of the account
   * @throws AccountNotFoundException if accountId does not exist
   */
  @Transactional
  public void deactivateAccount(UUID accountId) {
    var accountEntity = getAccountById(accountId);
    accountEntity.setStatus(AccountStatus.DISABLED);
    accountRepository.save(accountEntity);
  }

  // Utility

  private int resolveSearchLimit(Integer limit) {
    if (limit == null || limit < 1) {
      return DEFAULT_SEARCH_LIMIT;
    }
    return Math.min(limit, MAX_SEARCH_LIMIT);
  }
}
