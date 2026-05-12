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

package com.scaledrop.sdiam.application.service

import com.scaledrop.sdiam.IntegrationTestBase
import com.scaledrop.sdiam.adapter.db.AccountEntity
import com.scaledrop.sdiam.adapter.db.AccountEntity.AccountStatus
import com.scaledrop.sdiam.adapter.db.AccountRepository
import com.scaledrop.sdiam.adapter.db.IdentityEntity
import com.scaledrop.sdiam.adapter.db.IdentityProvider
import com.scaledrop.sdiam.adapter.db.IdentityRepository
import com.scaledrop.sdiam.configuration.exception.AuthenticationFailedException
import java.time.OffsetDateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.transaction.annotation.Transactional

@Transactional
class AuthenticationServiceTest extends IntegrationTestBase {

  @Autowired
  private AuthenticationService authService

  @Autowired
  private AccountRepository accountRepository

  @Autowired
  private IdentityRepository identityRepository

  private AccountEntity persistAccount(String username) {
    return persistAccount(username, AccountStatus.ACTIVE)
  }

  private AccountEntity persistAccount(String username, AccountStatus status) {
    return accountRepository.save(AccountEntity.builder()
        .id(UUID.randomUUID())
        .username(username)
        .status(status)
        .lastLoginAt(OffsetDateTime.parse("2022-10-10T15:00:00Z"))
        .build())
  }

  private IdentityEntity persistGoogleIdentity(
      AccountEntity account, String providerSubject, String email) {
    return identityRepository.save(IdentityEntity.builder()
        .id(UUID.randomUUID())
        .account(account)
        .provider(IdentityProvider.GOOGLE)
        .providerSubject(providerSubject)
        .email(email)
        .emailVerified(true)
        .build())
  }

  private DefaultOAuth2User googleUser(String subject, String email, boolean emailVerified) {
    return new DefaultOAuth2User(
        [
          new SimpleGrantedAuthority("ROLE_USER")
        ] as Set,
        [
          sub           : subject,
          email         : email,
          email_verified: emailVerified
        ],
        "sub")
  }
}
