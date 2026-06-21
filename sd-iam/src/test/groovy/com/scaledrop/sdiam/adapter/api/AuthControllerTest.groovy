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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.scaledrop.sdiam.WiremockTestBase
import com.scaledrop.sdiam.adapter.db.AccountEntity.AccountStatus
import com.scaledrop.sdiam.adapter.db.AccountRepository
import groovy.json.JsonSlurper
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.transaction.annotation.Transactional

@Transactional
class AuthControllerTest extends WiremockTestBase {

  private static final String GOOGLE_LOGIN_ENDPOINT = "/api/v1/login/google"

  @Autowired
  private AccountRepository accountRepository

  @SpringBean
  private GoogleIdTokenVerifier googleIdTokenVerifier = Mock()

  def "should return 200 OK and JWT for valid google token"() {
    given: "Valid google token payload"
    def email = "user@example.com"
    def subject = "google-123"
    def tokenString = "valid-test-token"

    googleIdTokenVerifier.verify(tokenString) >> mockGoogleToken(subject, email, true)

    def requestBody = """
    {
      "googleIdToken": "${tokenString}"
    }
    """

    when: "Performing POST request to login endpoint"
    def result = mockMvc.perform(post(GOOGLE_LOGIN_ENDPOINT)
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD))
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
        .andExpect(status().isOk())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then: "JWT is generated and returned"
    response.jwt != null
    response.jwt instanceof String

    and: "Account is auto-provisioned in DB during the process"
    accountRepository.findByUsernameAndStatusNot(email, AccountStatus.DISABLED).isPresent()
  }

  def "should return 401 UNAUTHORIZED when google token is invalid"() {
    given: "Invalid google token"
    def tokenString = "invalid-test-token"
    googleIdTokenVerifier.verify(tokenString) >> null

    def requestBody = """
    {
      "googleIdToken": "${tokenString}"
    }
    """

    when: "Performing POST request to login endpoint"
    def result = mockMvc.perform(post(GOOGLE_LOGIN_ENDPOINT)
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD))
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
        .andExpect(status().isUnauthorized())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then: "Proper error structure is returned"
    response.type == "UNAUTHORIZED"
    response.message == "Failed to authenticate with Google: Invalid or expired Google ID token"
  }

  def "should return 400 BAD REQUEST when googleIdToken is missing in request"() {
    given: "Empty request body"
    def requestBody = "{}"

    when: "Performing POST request"
    def result = mockMvc.perform(post(GOOGLE_LOGIN_ENDPOINT)
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD))
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
        .andExpect(status().isBadRequest())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then: "Validation error is returned"
    response.type == "VALIDATION"
    response.message == "Validation error"

    response.errors.find { it.field == "googleIdToken" } != null
  }

  // --- Helper Methods ---

  private GoogleIdToken mockGoogleToken(String subject, String email, Boolean emailVerified) {
    def payload = new GoogleIdToken.Payload()
    payload.setSubject(subject)
    payload.setEmail(email)
    payload.setEmailVerified(emailVerified)

    def token = Mock(GoogleIdToken)
    token.getPayload() >> payload
    return token
  }

  private static Object parseJson(String body) {
    return new JsonSlurper().parseText(body)
  }
}
