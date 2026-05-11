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

import static com.scaledrop.sdiam.configuration.security.JwtTokenService.ACCOUNT_ID_CLAIM
import static com.scaledrop.sdiam.configuration.security.JwtTokenService.EXPIRES_AT_CLAIM
import static java.nio.charset.StandardCharsets.UTF_8
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import com.scaledrop.sdiam.WiremockTestBase
import com.scaledrop.sdiam.adapter.db.AccountEntity
import com.scaledrop.sdiam.adapter.db.AccountEntity.AccountStatus
import com.scaledrop.sdiam.adapter.db.AccountRepository
import com.scaledrop.sdiam.application.service.AccountPasswordService
import com.scaledrop.sdiam.configuration.security.properties.JwtProperties
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.time.OffsetDateTime
import java.util.UUID
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
class SessionControllerTest extends WiremockTestBase {

  private static final String SESSION_ENDPOINT = "/api/v1/session"

  @Autowired
  private AccountRepository accountRepository

  @Autowired
  private AccountPasswordService accountPasswordService

  @Autowired
  private JwtProperties jwtProperties

  def "should issue jwt for valid local credentials without creating session"() {
    given:
    def account = persistAccount("test_username", "test_password1A!", AccountStatus.ACTIVE, null)

    when:
    def result = mockMvc.perform(post("${SESSION_ENDPOINT}/login")
        .contentType(APPLICATION_JSON)
        .content(toJson([
          username: "test_username",
          password: "test_password1A!"
        ])))
        .andExpect(status().isOk())
        .andReturn()

    def response = parseJson(result.response.contentAsString)
    def claims = parseJwtClaims(response.jwt as String)

    then:
    result.request.getSession(false) == null
    result.response.getCookie("JSESSIONID") == null
    response.keySet() == ["jwt"] as Set
    claims[ACCOUNT_ID_CLAIM] == account.id.toString()
    claims[EXPIRES_AT_CLAIM] == "2022-10-11T15:00:00Z"
  }

  def "should reject current session request because jwt is not consumed by this service"() {
    expect:
    mockMvc.perform(get(SESSION_ENDPOINT))
        .andExpect(status().isUnauthorized())
  }

  def "should expose google login redirect endpoint without authentication"() {
    expect:
    mockMvc.perform(get("${SESSION_ENDPOINT}/google"))
        .andExpect(status().isFound())
        .andExpect(redirectedUrl("/oauth2/authorization/google"))
  }

  def "should reject invalid local credentials"() {
    given:
    persistAccount("test_username", "test_password1A!", AccountStatus.ACTIVE, null)

    expect:
    mockMvc.perform(post("${SESSION_ENDPOINT}/login")
        .contentType(APPLICATION_JSON)
        .content(toJson([
          username: "test_username",
          password: "wrong_password"
        ])))
        .andExpect(status().isUnauthorized())
  }

  def "should issue jwt for active account when disabled account has same username"() {
    given:
    persistAccount("test_username", "disabled_password1A!", AccountStatus.DISABLED, null)
    def activeAccount = persistAccount("test_username", "test_password1A!", AccountStatus.ACTIVE, null)

    when:
    def result = mockMvc.perform(post("${SESSION_ENDPOINT}/login")
        .contentType(APPLICATION_JSON)
        .content(toJson([
          username: "test_username",
          password: "test_password1A!"
        ])))
        .andExpect(status().isOk())
        .andReturn()

    def response = parseJson(result.response.contentAsString)
    def claims = parseJwtClaims(response.jwt as String)

    then:
    claims[ACCOUNT_ID_CLAIM] == activeAccount.id.toString()
  }

  private AccountEntity persistAccount(
      String username,
      String plainPassword,
      AccountStatus status,
      OffsetDateTime lockedUntil) {
    def passwordData = accountPasswordService.hashPassword(plainPassword)
    return accountRepository.save(AccountEntity.builder()
        .id(UUID.randomUUID())
        .username(username)
        .passwordHash(passwordData.hash())
        .passwordSalt(passwordData.salt())
        .status(status)
        .lockedUntil(lockedUntil)
        .failedLoginAttempts(0)
        .build())
  }

  private String toJson(Object value) {
    return objectMapper.writeValueAsString(value)
  }

  private Map parseJson(String value) {
    return objectMapper.readValue(value, Map)
  }

  private Map parseJwtClaims(String jwt) {
    return Jwts.parser()
        .verifyWith(Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(UTF_8)))
        .build()
        .parseSignedClaims(jwt)
        .getPayload()
  }
}
