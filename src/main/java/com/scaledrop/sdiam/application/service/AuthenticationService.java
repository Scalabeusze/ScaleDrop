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

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.scaledrop.sdiam.adapter.db.AccountEntity;
import com.scaledrop.sdiam.adapter.db.AccountEntity.AccountStatus;
import com.scaledrop.sdiam.adapter.db.AccountRepository;
import com.scaledrop.sdiam.adapter.db.IdentityEntity;
import com.scaledrop.sdiam.adapter.db.IdentityProvider;
import com.scaledrop.sdiam.adapter.db.IdentityRepository;
import com.scaledrop.sdiam.configuration.exception.AccountNotFoundException;
import com.scaledrop.sdiam.configuration.exception.AuthenticationFailedException;
import com.scaledrop.sdiam.configuration.security.SessionAccountPrincipal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  final String INVALID_CREDENTIALS = "Invalid credentials";
  static final String ACCOUNT_DISABLED = "Account is disabled";
  static final String ACCOUNT_LOCKED = "Account is locked";
  private static final String GOOGLE_EMAIL_NOT_VERIFIED = "Google account email must be verified";
  private static final String GOOGLE_SUBJECT_MISSING = "Google account subject is missing";

  private final AccountRepository accountRepository;
  private final IdentityRepository identityRepository;
  private final AccountService accountService;
  private final AccountPasswordService hashingService;
  private final GoogleIdTokenVerifier googleIdTokenVerifier;
  final Clock clock;

  public SessionAccountPrincipal authGoogleWithToken(String idTokenString) {
    try {
      // Verify
      GoogleIdToken token = googleIdTokenVerifier.verify(idTokenString);
      if (token == null) {
        throw new AuthenticationFailedException("Invalid or expired Google ID token");
      }

      // Extract data
      GoogleIdToken.Payload payload = token.getPayload();
      String subject = payload.getSubject();
      String email = payload.getEmail();
      Boolean emailVerified = payload.getEmailVerified();

      if (StringUtils.isBlank(subject)) {
        throw new AuthenticationFailedException(GOOGLE_SUBJECT_MISSING);
      }
      if (emailVerified == null || !emailVerified || StringUtils.isBlank(email)) {
        throw new AuthenticationFailedException(GOOGLE_EMAIL_NOT_VERIFIED);
      }

      var existingIdentity = identityRepository.findByProviderAndProviderSubject(IdentityProvider.GOOGLE, subject);

      AccountEntity account;
      if (existingIdentity.isPresent()) {
        account = existingIdentity.get().getAccount();
      } else {
        account = accountRepository.findByUsernameAndStatusNot(email, AccountStatus.DISABLED).orElseGet(() -> autoProvisionAccount(email));
        accountService.validateAccountState(this, account);

        identityRepository.save(
            IdentityEntity.builder()
                .id(UUID.randomUUID())
                .account(account)
                .provider(IdentityProvider.GOOGLE)
                .providerSubject(subject)
                .email(email)
                .emailVerified(true)
                .build());
      }

      accountService.validateAccountState(this, account);
      account.setLastLoginAt(OffsetDateTime.now(clock));
      account = accountRepository.save(account);

      return SessionAccountPrincipal.from(account, IdentityProvider.GOOGLE);

    } catch (Exception e) {
      throw new AuthenticationFailedException("Failed to authenticate with Google: " + e.getMessage());
    }
  }

  @Transactional
  public SessionAccountPrincipal authLocal(String username, String plainPassword) {
    try {
      AccountEntity account = accountService.getAccountByUsername(username);
      accountService.validateAccountState(this, account);

      if (!hashingService.matchesPassword(
          plainPassword, account.getPasswordHash(), account.getPasswordSalt())) {
        throw new AuthenticationFailedException(INVALID_CREDENTIALS);
      }

      account.setLastLoginAt(OffsetDateTime.now(clock));
      accountRepository.save(account);
      return SessionAccountPrincipal.from(account, IdentityProvider.LOCAL);
    } catch (AccountNotFoundException exception) {
      throw new AuthenticationFailedException(INVALID_CREDENTIALS);
    }
  }

  @Transactional
  public SessionAccountPrincipal authGoogle(OAuth2User oauth2User) {
    String subject = oauth2User.getAttribute("sub");
    String email = oauth2User.getAttribute("email");
    Boolean emailVerified = oauth2User.getAttribute("email_verified");

    if (StringUtils.isBlank(subject)) {
      throw new AuthenticationFailedException(GOOGLE_SUBJECT_MISSING);
    }
    if (!emailVerified || StringUtils.isBlank(email)) {
      throw new AuthenticationFailedException(GOOGLE_EMAIL_NOT_VERIFIED);
    }

    var existingIdentity =
        identityRepository.findByProviderAndProviderSubject(IdentityProvider.GOOGLE, subject);

    AccountEntity account;
    if (existingIdentity.isPresent()) {
      var identity = existingIdentity.get();
      account = identity.getAccount();
      // Reuse old Google identity by assigning it to the newer account
      if (account.getStatus() == AccountStatus.DISABLED) {
        account =
            accountRepository
                .findByUsernameAndStatusNot(email, AccountStatus.DISABLED)
                .orElseGet(() -> autoProvisionAccount(email));
        accountService.validateAccountState(this, account);
        identity.setAccount(account);
        identity.setEmail(email);
        identity.setEmailVerified(true);
        identityRepository.save(identity);
      }
    } else {
      // link email to a local account, if such an account exists
      account =
          accountRepository
              .findByUsernameAndStatusNot(email, AccountStatus.DISABLED)
              .orElseGet(() -> autoProvisionAccount(email));
      accountService.validateAccountState(this, account);
      identityRepository.save(
          IdentityEntity.builder()
              .id(UUID.randomUUID())
              .account(account)
              .provider(IdentityProvider.GOOGLE)
              .providerSubject(subject)
              .email(email)
              .emailVerified(true)
              .build());
    }

    accountService.validateAccountState(this, account);
    account.setLastLoginAt(OffsetDateTime.now(clock));
    accountRepository.save(account);
    return SessionAccountPrincipal.from(account, IdentityProvider.GOOGLE);
  }

  private AccountEntity autoProvisionAccount(String email) {
    var placeholderPassword =
        hashingService.hashPassword(UUID.randomUUID() + "-" + UUID.randomUUID());
    AccountEntity account =
        accountRepository.save(
            AccountEntity.builder()
                .id(UUID.randomUUID())
                .username(email)
                .passwordHash(placeholderPassword.hash())
                .passwordSalt(placeholderPassword.salt())
                .status(AccountStatus.ACTIVE)
                .failedLoginAttempts(0)
                .build());
    return account;
  }
}
