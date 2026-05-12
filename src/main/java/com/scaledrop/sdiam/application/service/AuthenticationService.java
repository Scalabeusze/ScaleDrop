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
import com.scaledrop.sdiam.configuration.exception.AuthenticationFailedException;
import com.scaledrop.sdiam.configuration.security.SessionAccountPrincipal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  static final String ACCOUNT_DISABLED = "Account is disabled";
  private static final String GOOGLE_EMAIL_NOT_VERIFIED = "Google account email must be verified";
  private static final String GOOGLE_SUBJECT_MISSING = "Google account subject is missing";

  private final AccountRepository accountRepository;
  private final IdentityRepository identityRepository;
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

      var existingIdentity =
          identityRepository.findByProviderAndProviderSubject(IdentityProvider.GOOGLE, subject);

      AccountEntity account;
      if (existingIdentity.isPresent()) {
        account = existingIdentity.get().getAccount();
      } else {
        account =
            accountRepository
                .findByUsernameAndStatusNot(email, AccountStatus.DISABLED)
                .orElseGet(() -> autoProvisionAccount(email));
        validateAccountState(account);

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

      validateAccountState(account);
      account.setLastLoginAt(OffsetDateTime.now(clock));
      account = accountRepository.save(account);

      return SessionAccountPrincipal.from(account, IdentityProvider.GOOGLE);

    } catch (Exception e) {
      throw new AuthenticationFailedException(
          "Failed to authenticate with Google: " + e.getMessage());
    }
  }

  private static void validateAccountState(AccountEntity account) {
    if (account.getStatus() == AccountStatus.DISABLED) {
      throw new AuthenticationFailedException(AuthenticationService.ACCOUNT_DISABLED);
    }
  }

  private AccountEntity autoProvisionAccount(String email) {
    return accountRepository.save(
        AccountEntity.builder()
            .id(UUID.randomUUID())
            .username(email)
            .status(AccountStatus.ACTIVE)
            .build());
  }
}
