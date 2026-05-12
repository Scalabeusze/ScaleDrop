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

package com.scaledrop.sdiam.adapter.api.controller

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import com.scaledrop.sdiam.WiremockTestBase
import com.scaledrop.sdiam.adapter.db.AccountEntity
import com.scaledrop.sdiam.adapter.db.AccountEntity.AccountStatus
import com.scaledrop.sdiam.adapter.db.AccountRepository
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.transaction.annotation.Transactional

@Transactional
class AccountControllerTest extends WiremockTestBase {

  private static final String ACCOUNTS_ENDPOINT = "/api/v1/accounts"

  @Autowired
  private AccountRepository accountRepository

  def "should fetch account by username"() {
    given: "An existing account"
    def email = "marian@swiatwgkiepskich.com"
    persistAccount(email, "Marian", "Pazdzioch")

    when: "Requesting account by username parameter"
    def result = mockMvc.perform(get(ACCOUNTS_ENDPOINT)
        .param("username", email)
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isOk())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then: "Correct account details are returned"
    response.username == email
    response.firstName == "Marian"
    response.lastName == "Pazdzioch"
    response.status == "ACTIVE"
  }

  def "should get account by id"() {
    given:
    def account = persistAccount("test@example.com", "Test", "User")

    when:
    def result = mockMvc.perform(get("${ACCOUNTS_ENDPOINT}/${account.id}")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isOk())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response.id == account.id.toString()
    response.username == "test@example.com"
    response.firstName == "Test"
  }

  def "should update account profile fields"() {
    given: "Existing account"
    def account = persistAccount("user@example.com", "OldName", "OldSurname")

    when: "Updating profile fields via PUT"
    def result = mockMvc.perform(put("${ACCOUNTS_ENDPOINT}/${account.id}")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD))
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson([
          firstName: "NewName",
          lastName : "NewSurname",
          avatarUrl: "https://new-avatar.com"
        ])))
        .andExpect(status().isOk())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then: "Response contains updated data"
    response.firstName == "NewName"
    response.lastName == "NewSurname"
    response.avatarUrl == "https://new-avatar.com"

    and: "Database is updated"
    def updated = accountRepository.findById(account.id).get()
    updated.firstName == "NewName"
    updated.lastName == "NewSurname"
  }

  def "should search active accounts by username"() {
    given:
    def alpha = persistAccount("alpha@example.com", "Alpha", "User")
    persistAccount("beta@example.com", "Beta", "User")
    persistAccount("archived-alpha@example.com", "Old", "Alpha", AccountStatus.DISABLED)

    when:
    def result = mockMvc.perform(get("${ACCOUNTS_ENDPOINT}/search")
        .param("query", "ALPHA")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isOk())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then: "Only active matching accounts are returned with minimal data"
    response.size() == 1
    response[0].username == "alpha@example.com"
    response[0].id == alpha.id.toString()
    !response[0].containsKey("firstName") // Search response should be minimal
  }

  def "should disable account instead of delete"() {
    given:
    def account = persistAccount("to-be-disabled@example.com", "To", "Delete")

    when:
    mockMvc.perform(delete("${ACCOUNTS_ENDPOINT}/${account.id}")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isNoContent())

    then: "Account status in DB is changed to DISABLED"
    def deletedAccount = accountRepository.findById(account.id).orElseThrow()
    deletedAccount.status == AccountStatus.DISABLED
  }

  def "should return not found for missing account"() {
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

  // --- Helper Methods ---

  private AccountEntity persistAccount(String email, String fName = "John", String lName = "Doe", AccountStatus status = AccountStatus.ACTIVE) {
    return accountRepository.save(AccountEntity.builder()
        .id(UUID.randomUUID())
        .username(email) // Email as unique username
        .firstName(fName)
        .lastName(lName)
        .status(status)
        .build())
  }

  private static Object parseJson(String body) {
    return new JsonSlurper().parseText(body)
  }

  private String toJson(Map<String, ?> body) {
    return objectMapper.writeValueAsString(body)
  }
}
