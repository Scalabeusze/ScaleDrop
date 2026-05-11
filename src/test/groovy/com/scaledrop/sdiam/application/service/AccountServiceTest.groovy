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
import com.scaledrop.sdiam.adapter.api.model.request.CreateAccountAPIRequest
import com.scaledrop.sdiam.adapter.api.model.request.UpdateAccountAPIRequest
import com.scaledrop.sdiam.adapter.api.model.request.UpdatePasswordAPIRequest
import com.scaledrop.sdiam.adapter.db.AccountEntity
import com.scaledrop.sdiam.adapter.db.AccountEntity.AccountStatus
import com.scaledrop.sdiam.adapter.db.AccountRepository
import com.scaledrop.sdiam.configuration.exception.AccountConflictException
import com.scaledrop.sdiam.configuration.exception.AccountNotFoundException
import com.scaledrop.sdiam.configuration.exception.AccountValidationException
import java.time.OffsetDateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
class AccountServiceTest extends IntegrationTestBase {

  @Autowired
  private AccountService accountService

  @Autowired
  private AccountRepository accountRepository

  def "should create account with active status by default"() {
    when:
    def account = accountService.createAccount(
        new CreateAccountAPIRequest("test_username", "test_password1A!", null, null))

    then:
    account.id != null
    account.username == "test_username"
    account.status == AccountStatus.ACTIVE
    account.failedLoginAttempts == 0
    account.passwordHash
    account.passwordSalt

    and:
    def persistedAccount = accountRepository.findById(account.id).orElseThrow()
    persistedAccount.username == "test_username"
    persistedAccount.status == AccountStatus.ACTIVE
  }

  def "should create account with explicit status"() {
    when:
    def account = accountService.createAccount(
        new CreateAccountAPIRequest(
        "test_username",
        "test_password1A!",
        AccountStatus.LOCKED,
        OffsetDateTime.parse("2026-04-20T09:30:00Z")))

    then:
    account.status == AccountStatus.LOCKED
    account.lockedUntil == OffsetDateTime.parse("2026-04-20T09:30:00Z")
  }

  def "should reject duplicate username during create"() {
    given:
    persistAccount("test_username")

    when:
    accountService.createAccount(new CreateAccountAPIRequest("test_username", "test_password1A!", null, null))

    then:
    thrown(AccountConflictException)
  }

  def "should create account with username from disabled account"() {
    given:
    def disabledAccount = persistAccount("test_username", AccountStatus.DISABLED)

    when:
    def account = accountService.createAccount(
        new CreateAccountAPIRequest("test_username", "test_password1A!", null, null))

    then:
    account.id != disabledAccount.id
    account.username == "test_username"
    account.status == AccountStatus.ACTIVE
  }

  def "should get account by id"() {
    given:
    def account = persistAccount("test_username")

    expect:
    accountService.getAccountById(account.id).id == account.id
  }

  def "should get account by username"() {
    given:
    persistAccount("test_username")

    expect:
    accountService.getAccountByUsername("test_username").username == "test_username"
  }

  def "should not get disabled account by username"() {
    given:
    persistAccount("test_username", AccountStatus.DISABLED)

    when:
    accountService.getAccountByUsername("test_username")

    then:
    thrown(AccountNotFoundException)
  }

  def "should return all accounts"() {
    given:
    persistAccount("test_username")
    persistAccount("test_username1")

    when:
    def accounts = accountService.getAccounts()

    then:
    accounts*.username.containsAll([
      "test_username",
      "test_username1"
    ])
    accounts.size() == 2
  }

  def "should search active accounts by username sorted by username"() {
    given:
    persistAccount("zeta@example.com")
    persistAccount("alpha@example.com")
    persistAccount("archived-alpha@example.com", AccountStatus.DISABLED)
    persistAccount("locked-alpha@example.com", AccountStatus.LOCKED)

    when:
    def accounts = accountService.searchAccounts("ALPHA", 10)

    then:
    accounts*.username == ["alpha@example.com"]
  }

  def "should cap account search limit"() {
    given:
    (1..25).each {
      persistAccount(String.format("user-%02d@example.com", it))
    }

    when:
    def accounts = accountService.searchAccounts("user-", 50)

    then:
    accounts.size() == 20
    accounts*.username.first() == "user-01@example.com"
    accounts*.username.last() == "user-20@example.com"
  }

  def "should reject invalid account search query"() {
    when:
    accountService.searchAccounts(query, 10)

    then:
    thrown(AccountValidationException)

    where:
    query << [null, " ", "a", "a" * 101]
  }

  def "should update only provided fields"() {
    given:
    def account = persistAccount("test_username")
    def lastLoginAt = account.lastLoginAt

    when:
    def updatedAccount = accountService.updateAccount(
        account.id,
        new UpdateAccountAPIRequest(
        "test_username1",
        AccountStatus.DISABLED,
        2,
        OffsetDateTime.parse("2026-04-20T11:00:00Z"),
        null))

    then:
    updatedAccount.username == "test_username1"
    updatedAccount.status == AccountStatus.DISABLED
    updatedAccount.failedLoginAttempts == 2
    updatedAccount.lockedUntil == OffsetDateTime.parse("2026-04-20T11:00:00Z")
    updatedAccount.lastLoginAt == lastLoginAt
  }

  def "should reject duplicate username during update"() {
    given:
    def account = persistAccount("test_username")
    persistAccount("test_username1")

    when:
    accountService.updateAccount(
        account.id,
        new UpdateAccountAPIRequest("test_username1", null, null, null, null))

    then:
    thrown(AccountConflictException)
  }

  def "should reject enabling disabled account when username is used by active account"() {
    given:
    def disabledAccount = persistAccount("test_username", AccountStatus.DISABLED)
    persistAccount("test_username", AccountStatus.ACTIVE)

    when:
    accountService.updateAccount(
        disabledAccount.id,
        new UpdateAccountAPIRequest(null, AccountStatus.ACTIVE, null, null, null))

    then:
    thrown(AccountConflictException)
  }

  def "should update password and password updated timestamp"() {
    given:
    def account = persistAccount("test_username")
    def previousHash = account.passwordHash
    def previousSalt = account.passwordSalt

    when:
    def updatedAccount =
        accountService.updatePassword(account.id, new UpdatePasswordAPIRequest("test_password2A!"))

    then:
    updatedAccount.passwordHash != previousHash
    updatedAccount.passwordSalt != previousSalt
    updatedAccount.passwordUpdatedAt.toInstant() == OffsetDateTime.parse("2022-10-10T15:00:00Z").toInstant()
  }

  def "should disable account instead of delete"() {
    given:
    def account = persistAccount("test_username")

    when:
    accountService.deleteAccount(account.id)

    then:
    def deletedAccount = accountRepository.findById(account.id).orElseThrow()
    deletedAccount.status == AccountStatus.DISABLED
  }

  def "should throw not found for missing account by id"() {
    when:
    accountService.getAccountById(UUID.randomUUID())

    then:
    thrown(AccountNotFoundException)
  }

  def "should throw not found for missing account by username"() {
    when:
    accountService.getAccountByUsername("test_username_missing")

    then:
    thrown(AccountNotFoundException)
  }

  private AccountEntity persistAccount(String username) {
    return persistAccount(username, AccountStatus.ACTIVE)
  }

  private AccountEntity persistAccount(String username, AccountStatus status) {
    return accountRepository.save(AccountEntity.builder()
        .id(UUID.randomUUID())
        .username(username)
        .passwordHash("hash-${username}")
        .passwordSalt("salt-${username}")
        .status(status)
        .failedLoginAttempts(0)
        .build())
  }
}
