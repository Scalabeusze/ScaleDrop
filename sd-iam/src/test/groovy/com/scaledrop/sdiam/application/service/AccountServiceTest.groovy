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

  def "should update only provided profile fields"() {
    given: "An existing account with initial profile data"
    def initialEmail = "marian@swiatwgkiepskich.com"
    def account = accountRepository.save(AccountEntity.builder()
        .id(UUID.randomUUID())
        .username(initialEmail)
        .firstName("Marian")
        .lastName("Pazdzioch")
        .status(AccountStatus.ACTIVE)
        .build())

    when: "Updating only the first name and adding an avatar"
    def request = new UpdateAccountAPIRequest(
        "Marek",           // New first name
        null,              // Last name remains unchanged
        "https://avatar.io" // New avatar URL
        )
    def updatedAccount = accountService.updateAccount(account.id, request)

    then: "Only requested fields are updated"
    updatedAccount.firstName == "Marek"
    updatedAccount.avatarUrl == "https://avatar.io"

    and: "Other fields remain untouched"
    updatedAccount.lastName == "Pazdzioch"
    updatedAccount.username == initialEmail
    updatedAccount.status == AccountStatus.ACTIVE
  }

  def "should disable account instead of delete"() {
    given:
    def account = persistAccount("test_username")

    when:
    accountService.deactivateAccount(account.id)

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
