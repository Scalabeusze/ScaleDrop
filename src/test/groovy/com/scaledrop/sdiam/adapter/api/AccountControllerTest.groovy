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

package com.scaledrop.sdiam.adapter.api

import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import com.scaledrop.sdiam.WiremockTestBase
import com.scaledrop.sdiam.adapter.db.AccountEntity
import com.scaledrop.sdiam.adapter.db.AccountEntity.AccountStatus
import com.scaledrop.sdiam.adapter.db.AccountRepository
import groovy.json.JsonSlurper
import java.time.OffsetDateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
class AccountControllerTest extends WiremockTestBase {

  private static final String ACCOUNTS_ENDPOINT = "/api/v1/accounts"
  private static final String PASSWORD = "test_password1A!"

  @Autowired
  private AccountRepository accountRepository

  def "should create account"() {
    when:
    def result = mockMvc.perform(post(ACCOUNTS_ENDPOINT)
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD))
        .contentType(APPLICATION_JSON)
        .content(toJson([
          username     : "test_username",
          plainPassword: PASSWORD,
          status       : "LOCKED",
          lockedUntil  : "2026-04-20T09:30:00Z"
        ])))
        .andExpect(status().isCreated())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response.id
    response.username == "test_username"
    response.status == "LOCKED"
    response.failedLoginAttempts == 0
    response.lockedUntil == "2026-04-20T09:30:00Z"
    response.passwordUpdatedAt == null
    response.createdAt
    response.updatedAt

    and:
    def savedAccount = accountRepository.findByUsername("test_username").orElseThrow()
    savedAccount.status == AccountStatus.LOCKED
    savedAccount.passwordHash
    savedAccount.passwordSalt
  }

  def "should reject invalid create account request"() {
    when:
    def result = mockMvc.perform(post(ACCOUNTS_ENDPOINT)
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD))
        .contentType(APPLICATION_JSON)
        .content(toJson([
          username     : " ",
          plainPassword: "short"
        ])))
        .andExpect(status().isBadRequest())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response.type == "VALIDATION"
    response.message == "Validation error"
    response.errors*.field.contains("username")
    response.errors*.field.contains("plainPassword")
  }

  def "should reject create account with duplicate username"() {
    given:
    persistAccount("test_username")

    when:
    def result = mockMvc.perform(post(ACCOUNTS_ENDPOINT)
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD))
        .contentType(APPLICATION_JSON)
        .content(toJson([
          username     : "test_username",
          plainPassword: PASSWORD
        ])))
        .andExpect(status().isConflict())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response.type == "CONFLICT"
    response.message == "Username already exists"
  }

  def "should list accounts"() {
    given:
    persistAccount("test_username")
    persistAccount("test_username1")

    when:
    def result = mockMvc.perform(get(ACCOUNTS_ENDPOINT)
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isOk())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response*.username as Set == [
      "test_username",
      "test_username1"
    ] as Set
    response*.status as Set == ["ACTIVE"] as Set
    response.size() == 2
  }

  def "should get account by id"() {
    given:
    def account = persistAccount("test_username")

    when:
    def result = mockMvc.perform(get("${ACCOUNTS_ENDPOINT}/${account.id}")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isOk())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response.id == account.id.toString()
    response.username == "test_username"
    response.status == "ACTIVE"
  }

  def "should return not found when account does not exist"() {
    when:
    def result = mockMvc.perform(get("${ACCOUNTS_ENDPOINT}/${UUID.randomUUID()}")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isNotFound())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response.type == "NOT_FOUND"
    response.message == "Account not found"
  }

  def "should update account"() {
    given:
    def account = persistAccount("test_username")

    when:
    def result = mockMvc.perform(put("${ACCOUNTS_ENDPOINT}/${account.id}")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD))
        .contentType(APPLICATION_JSON)
        .content(toJson([
          username           : "test_username1",
          status             : "DISABLED",
          failedLoginAttempts: 3,
          lockedUntil        : "2026-04-20T10:00:00Z",
          lastLoginAt        : "2026-04-19T10:15:00Z"
        ])))
        .andExpect(status().isOk())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response.id == account.id.toString()
    response.username == "test_username1"
    response.status == "DISABLED"
    response.failedLoginAttempts == 3
    response.lockedUntil == "2026-04-20T10:00:00Z"
    response.lastLoginAt == "2026-04-19T10:15:00Z"

    and:
    def updatedAccount = accountRepository.findById(account.id).orElseThrow()
    updatedAccount.username == "test_username1"
    updatedAccount.status == AccountStatus.DISABLED
    updatedAccount.failedLoginAttempts == 3
  }

  def "should reject update account with duplicate username"() {
    given:
    def account = persistAccount("test_username")
    persistAccount("test_username1")

    when:
    def result = mockMvc.perform(put("${ACCOUNTS_ENDPOINT}/${account.id}")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD))
        .contentType(APPLICATION_JSON)
        .content(toJson([
          username: "test_username1"
        ])))
        .andExpect(status().isConflict())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response.type == "CONFLICT"
    response.message == "Username already exists"
  }

  def "should reject invalid update account request"() {
    given:
    def account = persistAccount("test_username")

    when:
    def result = mockMvc.perform(put("${ACCOUNTS_ENDPOINT}/${account.id}")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD))
        .contentType(APPLICATION_JSON)
        .content(toJson([
          username: " "
        ])))
        .andExpect(status().isBadRequest())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response.type == "VALIDATION"
    response.message == "Validation error"
  }

  def "should return not found when updating missing account"() {
    when:
    def result = mockMvc.perform(put("${ACCOUNTS_ENDPOINT}/${UUID.randomUUID()}")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD))
        .contentType(APPLICATION_JSON)
        .content(toJson([
          username: "test_username1"
        ])))
        .andExpect(status().isNotFound())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response.type == "NOT_FOUND"
    response.message == "Account not found"
  }

  def "should update password"() {
    given:
    def account = persistAccount("test_username")
    def previousHash = account.passwordHash
    def previousSalt = account.passwordSalt

    when:
    def result = mockMvc.perform(patch("${ACCOUNTS_ENDPOINT}/${account.id}/password")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD))
        .contentType(APPLICATION_JSON)
        .content(toJson([
          plainPassword: "test_password2A!"
        ])))
        .andExpect(status().isOk())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response.id == account.id.toString()
    OffsetDateTime.parse(response.passwordUpdatedAt).toInstant() == OffsetDateTime.parse("2022-10-10T15:00:00Z").toInstant()

    and:
    def updatedAccount = accountRepository.findById(account.id).orElseThrow()
    updatedAccount.passwordUpdatedAt.toInstant() == OffsetDateTime.parse("2022-10-10T15:00:00Z").toInstant()
    updatedAccount.passwordHash != previousHash
    updatedAccount.passwordSalt != previousSalt
  }

  def "should reject invalid password update request"() {
    given:
    def account = persistAccount("test_username")

    when:
    def result = mockMvc.perform(patch("${ACCOUNTS_ENDPOINT}/${account.id}/password")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD))
        .contentType(APPLICATION_JSON)
        .content(toJson([
          plainPassword: "short"
        ])))
        .andExpect(status().isBadRequest())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response.type == "VALIDATION"
    response.message == "Validation error"
  }

  def "should return not found when updating password for missing account"() {
    when:
    def result = mockMvc.perform(patch("${ACCOUNTS_ENDPOINT}/${UUID.randomUUID()}/password")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD))
        .contentType(APPLICATION_JSON)
        .content(toJson([
          plainPassword: "test_password2A!"
        ])))
        .andExpect(status().isNotFound())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response.type == "NOT_FOUND"
    response.message == "Account not found"
  }

  def "should delete account"() {
    given:
    def account = persistAccount("test_username")

    when:
    mockMvc.perform(delete("${ACCOUNTS_ENDPOINT}/${account.id}")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isNoContent())

    then:
    !accountRepository.existsByUsername(account.username)
  }

  def "should return not found when deleting missing account"() {
    when:
    def result = mockMvc.perform(delete("${ACCOUNTS_ENDPOINT}/${UUID.randomUUID()}")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isNotFound())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response.type == "NOT_FOUND"
    response.message == "Account not found"
  }

  private AccountEntity persistAccount(String username) {
    return accountRepository.save(AccountEntity.builder()
        .id(UUID.randomUUID())
        .username(username)
        .passwordHash("hash-${username}")
        .passwordSalt("salt-${username}")
        .status(AccountStatus.ACTIVE)
        .failedLoginAttempts(0)
        .build())
  }

  private Object parseJson(String body) {
    return new JsonSlurper().parseText(body)
  }

  private String toJson(Map<String, ?> body) {
    return objectMapper.writeValueAsString(body)
  }
}
