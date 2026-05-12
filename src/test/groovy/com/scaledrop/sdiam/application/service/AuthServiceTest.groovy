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

  def "should link google identity to existing account matched by verified email"() {
    given:
    def account = persistAccount("user@example.com")
    def oauth2User = googleUser("google-subject", "user@example.com", true)

    when:
    def principal = authService.authGoogle(oauth2User)

    then:
    principal.accountId == account.id
    principal.provider == IdentityProvider.GOOGLE

    and:
    def identity = identityRepository.findByProviderAndProviderSubject(IdentityProvider.GOOGLE, "google-subject").orElseThrow()
    identity.account.id == account.id
    identity.email == "user@example.com"
  }

  def "should auto provision account for first google login"() {
    given:
    def oauth2User = googleUser("new-google-subject", "new-user@example.com", true)

    when:
    def principal = authService.authGoogle(oauth2User)

    then:
    principal.username == "new-user@example.com"
    principal.provider == IdentityProvider.GOOGLE

    and:
    def persistedAccount =
        accountRepository.findByUsernameAndStatusNot("new-user@example.com", AccountStatus.DISABLED).orElseThrow()
    persistedAccount.id == principal.accountId
    identityRepository.findByProviderAndProviderSubject(IdentityProvider.GOOGLE, "new-google-subject").isPresent()
  }

  def "should link google identity to active account when disabled account has same username"() {
    given:
    persistAccount("user@example.com", AccountStatus.DISABLED)
    def activeAccount = persistAccount("user@example.com", AccountStatus.ACTIVE)
    def oauth2User = googleUser("google-subject", "user@example.com", true)

    when:
    def principal = authService.authGoogle(oauth2User)

    then:
    principal.accountId == activeAccount.id
    principal.provider == IdentityProvider.GOOGLE

    and:
    def identity = identityRepository.findByProviderAndProviderSubject(IdentityProvider.GOOGLE, "google-subject").orElseThrow()
    identity.account.id == activeAccount.id
  }

  def "should move existing google identity from disabled account to active account"() {
    given:
    def disabledAccount = persistAccount("user@example.com", AccountStatus.DISABLED)
    def activeAccount = persistAccount("user@example.com", AccountStatus.ACTIVE)
    persistGoogleIdentity(disabledAccount, "google-subject", "user@example.com")
    def oauth2User = googleUser("google-subject", "user@example.com", true)

    when:
    def principal = authService.authGoogle(oauth2User)

    then:
    principal.accountId == activeAccount.id
    principal.provider == IdentityProvider.GOOGLE

    and:
    def identity = identityRepository.findByProviderAndProviderSubject(IdentityProvider.GOOGLE, "google-subject").orElseThrow()
    identity.account.id == activeAccount.id
    identity.email == "user@example.com"
    identity.emailVerified
  }

  def "should move existing google identity from disabled account to new account"() {
    given:
    def disabledAccount = persistAccount("user@example.com", AccountStatus.DISABLED)
    persistGoogleIdentity(disabledAccount, "google-subject", "user@example.com")
    def oauth2User = googleUser("google-subject", "user@example.com", true)

    when:
    def principal = authService.authGoogle(oauth2User)

    then:
    principal.username == "user@example.com"
    principal.provider == IdentityProvider.GOOGLE
    principal.accountId != disabledAccount.id

    and:
    def identity = identityRepository.findByProviderAndProviderSubject(IdentityProvider.GOOGLE, "google-subject").orElseThrow()
    identity.account.id == principal.accountId
    identity.email == "user@example.com"
    identity.emailVerified
  }

  def "should reject google login without verified email"() {
    when:
    authService.authGoogle(googleUser("google-subject", "user@example.com", false))

    then:
    thrown(AuthenticationFailedException)
  }

  private AccountEntity persistAccount(String username) {
    return persistAccount(username, AccountStatus.ACTIVE)
  }

  private AccountEntity persistAccount(String username, AccountStatus status) {
    return accountRepository.save(AccountEntity.builder()
        .id(UUID.randomUUID())
        .username(username)
        .status(status)
        .failedLoginAttempts(0)
        .lockedUntil(null)
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
