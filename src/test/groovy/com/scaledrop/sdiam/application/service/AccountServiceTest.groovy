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
import com.scaledrop.sdiam.adapter.api.model.request.UpdateAccountAPIRequest
import com.scaledrop.sdiam.adapter.db.AccountEntity
import com.scaledrop.sdiam.adapter.db.AccountEntity.AccountStatus
import com.scaledrop.sdiam.adapter.db.AccountRepository
import com.scaledrop.sdiam.configuration.exception.AccountConflictException
import com.scaledrop.sdiam.configuration.exception.AccountNotFoundException
import com.scaledrop.sdiam.configuration.exception.AccountValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
class AccountServiceTest extends IntegrationTestBase {

  @Autowired
  private AccountService accountService

  @Autowired
  private AccountRepository accountRepository

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
        null))

    then:
    updatedAccount.username == "test_username1"
    updatedAccount.status == AccountStatus.DISABLED
    updatedAccount.lastLoginAt == lastLoginAt
  }

  def "should reject duplicate username during update"() {
    given:
    def account = persistAccount("test_username")
    persistAccount("test_username1")

    when:
    accountService.updateAccount(
        account.id,
        new UpdateAccountAPIRequest("test_username1", null, null))

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
        new UpdateAccountAPIRequest(null, AccountStatus.ACTIVE, null))

    then:
    thrown(AccountConflictException)
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
        .status(status)
        .build())
  }
}
