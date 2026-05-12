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

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.scaledrop.sdiam.IntegrationTestBase
import com.scaledrop.sdiam.adapter.db.AccountEntity
import com.scaledrop.sdiam.adapter.db.AccountEntity.AccountStatus
import com.scaledrop.sdiam.adapter.db.AccountRepository
import com.scaledrop.sdiam.adapter.db.IdentityEntity
import com.scaledrop.sdiam.adapter.db.IdentityProvider
import com.scaledrop.sdiam.adapter.db.IdentityRepository
import com.scaledrop.sdiam.configuration.exception.AuthenticationFailedException
import java.time.OffsetDateTime
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
class AuthenticationServiceTest extends IntegrationTestBase {

  @Autowired
  private AuthenticationService authService

  @Autowired
  private AccountRepository accountRepository

  @Autowired
  private IdentityRepository identityRepository

  @SpringBean
  private GoogleIdTokenVerifier googleIdTokenVerifier = Mock()

  def "should authenticate successfully for existing identity"() {
    given:
    def email = "existing@example.com"
    def subject = "google-sub-123"
    def tokenString = "valid-token-string"

    def account = persistAccount(email)
    persistGoogleIdentity(account, subject, email)

    googleIdTokenVerifier.verify(tokenString) >> mockGoogleToken(subject, email, true)

    when:
    def principal = authService.authGoogleWithToken(tokenString)

    then: "Principal is returned successfully"
    principal.accountId == account.id
    principal.provider == IdentityProvider.GOOGLE

    and: "Last login date is updated"
    def updatedAccount = accountRepository.findById(account.id).get()
    !updatedAccount.lastLoginAt.toInstant().isBefore(OffsetDateTime.parse("2022-10-10T15:00:00Z").toInstant())
  }

  def "should link new google identity to existing account by email"() {
    given:
    def email = "match-me@example.com"
    def subject = "new-google-sub-456"
    def tokenString = "valid-token-string"

    // Account exists (e.g. from previous local registration), but no Google Identity
    def account = persistAccount(email)

    googleIdTokenVerifier.verify(tokenString) >> mockGoogleToken(subject, email, true)

    when:
    def principal = authService.authGoogleWithToken(tokenString)

    then: "Principal is linked to existing account"
    principal.accountId == account.id

    and: "New Google Identity is saved"
    def identity = identityRepository.findByProviderAndProviderSubject(IdentityProvider.GOOGLE, subject)
    identity.isPresent()
    identity.get().account.id == account.id
    identity.get().email == email
  }

  def "should auto-provision new account and identity for completely new user"() {
    given:
    def email = "brand-new@example.com"
    def subject = "google-sub-789"
    def tokenString = "valid-token-string"

    // No account and no identity exist in the DB

    googleIdTokenVerifier.verify(tokenString) >> mockGoogleToken(subject, email, true)

    when:
    def principal = authService.authGoogleWithToken(tokenString)

    then: "New account and identity are created"
    def newAccount = accountRepository.findByUsernameAndStatusNot(email, AccountStatus.DISABLED)
    newAccount.isPresent()
    newAccount.get().status == AccountStatus.ACTIVE

    principal.accountId == newAccount.get().id

    def identity = identityRepository.findByProviderAndProviderSubject(IdentityProvider.GOOGLE, subject)
    identity.isPresent()
    identity.get().account.id == newAccount.get().id
  }

  def "should throw exception when google token is invalid or expired"() {
    given:
    def tokenString = "invalid-token"
    googleIdTokenVerifier.verify(tokenString) >> null // Verifier returns null for bad tokens

    when:
    authService.authGoogleWithToken(tokenString)

    then:
    def e = thrown(AuthenticationFailedException)
    e.message.contains("Invalid or expired Google ID token")
  }

  def "should throw exception when subject is missing in token payload"() {
    given:
    def tokenString = "valid-token-string"
    googleIdTokenVerifier.verify(tokenString) >> mockGoogleToken(null, "test@example.com", true)

    when:
    authService.authGoogleWithToken(tokenString)

    then:
    def e = thrown(AuthenticationFailedException)
    e.message.contains("Google account subject is missing")
  }

  def "should throw exception when email is not verified"() {
    given:
    def tokenString = "valid-token-string"
    googleIdTokenVerifier.verify(tokenString) >> mockGoogleToken("sub-123", "test@example.com", false)

    when:
    authService.authGoogleWithToken(tokenString)

    then:
    def e = thrown(AuthenticationFailedException)
    e.message.contains("Google account email must be verified")
  }

  def "should throw exception when trying to log in to disabled account"() {
    given:
    def email = "banned@example.com"
    def subject = "google-sub-123"
    def tokenString = "valid-token-string"

    def account = persistAccount(email, AccountStatus.DISABLED)
    persistGoogleIdentity(account, subject, email)

    googleIdTokenVerifier.verify(tokenString) >> mockGoogleToken(subject, email, true)

    when:
    authService.authGoogleWithToken(tokenString)

    then:
    def e = thrown(AuthenticationFailedException)
    e.message.contains("Account is disabled")
  }

  // --- Helper Methods ---

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

  private GoogleIdToken mockGoogleToken(String subject, String email, Boolean emailVerified) {
    def payload = new GoogleIdToken.Payload()
    payload.setSubject(subject)
    payload.setEmail(email)
    payload.setEmailVerified(emailVerified)

    def token = Mock(GoogleIdToken)
    token.getPayload() >> payload
    return token
  }
}
