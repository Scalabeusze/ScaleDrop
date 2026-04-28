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

import com.scaledrop.sdiam.adapter.api.model.request.CreateAccountAPIRequest;
import com.scaledrop.sdiam.adapter.api.model.request.UpdateAccountAPIRequest;
import com.scaledrop.sdiam.adapter.api.model.request.UpdatePasswordAPIRequest;
import com.scaledrop.sdiam.adapter.db.AccountEntity;
import com.scaledrop.sdiam.adapter.db.AccountEntity.AccountStatus;
import com.scaledrop.sdiam.adapter.db.AccountRepository;
import com.scaledrop.sdiam.configuration.exception.AccountConflictException;
import com.scaledrop.sdiam.configuration.exception.AccountNotFoundException;
import com.scaledrop.sdiam.configuration.exception.AccountServiceException;
import com.scaledrop.sdiam.configuration.exception.AccountValidationException;
import com.scaledrop.sdiam.configuration.exception.AuthenticationFailedException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

  private static final String ACCOUNT_NOT_FOUND = "Account not found";
  private static final String USERNAME_ALREADY_EXISTS = "Username already exists";

  private final AccountRepository accountRepository;
  private final AccountPasswordService hashingService;
  private final Clock clock;

  // Create

  /**
   * Creates a brand new account
   *
   * @param req request payload
   * @return newly created AccountEntity
   * @throws AccountConflictException if username is taken
   * @throws AccountValidationException if new password does not meet validation requirements
   * @throws AccountServiceException if hashing service fails
   */
  @Transactional
  public AccountEntity createAccount(CreateAccountAPIRequest req) {
    if (accountRepository.existsByUsername(req.username())) {
      throw new AccountConflictException(USERNAME_ALREADY_EXISTS);
    }

    var passwordData = hashingService.hashPassword(req.plainPassword());

    var accountEntity =
        AccountEntity.builder()
            .id(UUID.randomUUID())
            .username(req.username())
            .passwordHash(passwordData.hash())
            .passwordSalt(passwordData.salt())
            .status(req.status() == null ? AccountStatus.ACTIVE : req.status())
            .lockedUntil(req.lockedUntil())
            .build();

    return accountRepository.save(accountEntity);
  }

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
        .findByUsername(username)
        .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_NOT_FOUND));
  }

  /**
   * Gets all accounts in the system
   *
   * @return List of AccountEntity
   */
  @Transactional(readOnly = true)
  public List<AccountEntity> getAccounts() {
    return accountRepository.findAll();
  }

  // Update

  /**
   * Updates the user's account
   *
   * @param accountId UUID of the account
   * @param req request payload
   * @return updated AccountEntity
   * @throws AccountNotFoundException if accountId does not exist
   * @throws AccountConflictException if username is taken
   */
  @Transactional
  public AccountEntity updateAccount(UUID accountId, UpdateAccountAPIRequest req) {
    var accountEntity = getAccountById(accountId);

    if (req.username() != null && !accountEntity.getUsername().equals(req.username())) {
      if (accountRepository.existsByUsername(req.username())) {
        throw new AccountConflictException(USERNAME_ALREADY_EXISTS);
      }
      accountEntity.setUsername(req.username());
    }

    if (req.status() != null) {
      accountEntity.setStatus(req.status());
    }

    if (req.lastLoginAt() != null) {
      accountEntity.setLastLoginAt(req.lastLoginAt());
    }

    if (req.failedLoginAttempts() != null) {
      accountEntity.setFailedLoginAttempts(req.failedLoginAttempts());
    }

    if (req.lockedUntil() != null) {
      accountEntity.setLockedUntil(req.lockedUntil());
    }

    return accountRepository.save(accountEntity);
  }

  /**
   * Updates the user's password
   *
   * @param accountId UUID of the account
   * @param req request payload
   * @return updated AccountEntity
   * @throws AccountNotFoundException if accountId does not exist
   * @throws AccountValidationException if new password does not meet validation requirements
   * @throws AccountServiceException if hashing service fails
   */
  @Transactional
  public AccountEntity updatePassword(UUID accountId, UpdatePasswordAPIRequest req) {
    var accountEntity = getAccountById(accountId);
    var passwordData = hashingService.hashPassword(req.plainPassword());

    accountEntity.setPasswordHash(passwordData.hash());
    accountEntity.setPasswordSalt(passwordData.salt());
    accountEntity.setPasswordUpdatedAt(OffsetDateTime.now(clock));

    return accountRepository.save(accountEntity);
  }

  // Delete

  /**
   * Deletes and account
   *
   * @param accountId UUID of the account
   * @throws AccountNotFoundException if accountId does not exist
   */
  @Transactional
  public void deleteAccount(UUID accountId) {
    var accountEntity = getAccountById(accountId);
    accountEntity.setStatus(AccountStatus.DISABLED);
    accountRepository.save(accountEntity);
  }

  // Utility

  /**
   * Validates account expiry status
   *
   * @param authenticationService
   * @param account
   */
  void validateAccountState(AuthenticationService authenticationService, AccountEntity account) {
    if (account.getStatus() == AccountStatus.DISABLED) {
      throw new AuthenticationFailedException(AuthenticationService.ACCOUNT_DISABLED);
    }
    if (account.getStatus() == AccountStatus.LOCKED
        && account.getLockedUntil() != null
        && account.getLockedUntil().isAfter(OffsetDateTime.now(authenticationService.clock))) {
      throw new AuthenticationFailedException(AuthenticationService.ACCOUNT_LOCKED);
    }
  }
}
