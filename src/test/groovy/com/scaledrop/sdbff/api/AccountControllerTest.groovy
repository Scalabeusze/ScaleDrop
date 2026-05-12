package com.scaledrop.sdbff.api

import com.scaledrop.sdbff.WiremockTestBase
import groovy.json.JsonSlurper
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AccountControllerTest extends WiremockTestBase {

  private static final String ACCOUNT_ENDPOINT = "/api/v1/account"
  private static final String LOGIN_ENDPOINT = "/api/v1/login"

  def "should login with google token and return jwt"() {
    given: "A valid google ID token"
      def googleToken = "valid.google.token"
      def generatedJwt = "signed.jwt.from.iam"

    and: "IAM Service is stubbed to return a JWT for this token"
      stubFor(post(urlEqualTo("/api/v1/login/google"))
          .withRequestBody(matchingJsonPath('$.googleIdToken', equalTo(googleToken)))
          .willReturn(aResponse()
              .withStatus(200)
              .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
              .withBody("""{"jwt": "${generatedJwt}"}""")))

    when: "Performing login request"
      def result = mockMvc.perform(MockMvcRequestBuilders.post(LOGIN_ENDPOINT)
          .contentType(MediaType.APPLICATION_JSON)
          .content("""{"googleIdToken": "${googleToken}"}"""))
          .andExpect(status().isOk())
          .andReturn()

    then: "JWT is returned to the client"
      def response = parseJson(result.response.contentAsString)
      response.jwt == generatedJwt
  }

  def "should return 401 when login fails"() {
    given: "A valid google ID token"
      def googleToken = "invalid.google.token"

    and: "IAM Service is stubbed to return a JWT for this token"
      stubFor(post(urlEqualTo("/api/v1/login/google"))
          .withRequestBody(matchingJsonPath('$.googleIdToken', equalTo(googleToken)))
          .willReturn(aResponse().withStatus(401)))

    when: "Performing login request"
      def result = mockMvc.perform(MockMvcRequestBuilders.post(LOGIN_ENDPOINT)
          .contentType(MediaType.APPLICATION_JSON)
          .content("""{"googleIdToken": "${googleToken}"}"""))
          .andExpect(status().isUnauthorized())
          .andReturn()

    then: "exception message is returned"
      def response = parseJson(result.response.contentAsString)
      response.uuid != null
      response.message != null
      response.type == "UNAUTHORIZED"
  }

  def "should fetch current user account details"() {
    given: "An authenticated user ID"
      def userId = UUID.randomUUID()

    and: "IAM Service is stubbed to return account details for this ID"
      stubFor(get(urlEqualTo("/api/v1/accounts/${userId}"))
          .willReturn(aResponse()
              .withStatus(200)
              .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
              .withBody("""{
                "id": "${userId}",
                "username": "test@example.com",
                "firstName": "John",
                "lastName": "Doe",
                "avatarUrl": "https://avatar.com/john",
                "status": "ACTIVE"
            }""")))

    when: "Fetching account endpoint as the authenticated user"
      def result = mockMvc.perform(MockMvcRequestBuilders.get(ACCOUNT_ENDPOINT)
          .with(jwt()
              .authorities(new SimpleGrantedAuthority("ROLE_USER"))
              .jwt { it.subject(userId.toString()) }
          )
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andReturn()

    then: "Account details are mapped and returned correctly"
      def response = parseJson(result.response.contentAsString)
      response.id == userId.toString()
      response.username == "test@example.com"
      response.firstName == "John"
      response.lastName == "Doe"
      response.avatarUrl == "https://avatar.com/john"
  }

  def "should return not found when account not found"() {
    given: "An authenticated user ID"
      def userId = UUID.randomUUID()

    and: "IAM Service is stubbed to return account details for this ID"
      stubFor(get(urlEqualTo("/api/v1/accounts/${userId}"))
          .willReturn(aResponse()
              .withStatus(404)))

    when: "Fetching account endpoint as the authenticated user"
      def result = mockMvc.perform(MockMvcRequestBuilders.get(ACCOUNT_ENDPOINT)
          .with(jwt()
              .authorities(new SimpleGrantedAuthority("ROLE_USER"))
              .jwt { it.subject(userId.toString()) }
          )
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound())
          .andReturn()

    then: "exception message is returned"
      def response = parseJson(result.response.contentAsString)
      response.uuid != null
      response.message != null
      response.type == "NOT_FOUND"
  }

  def "should return external server error when IAM fails"() {
    given: "An authenticated user ID"
      def userId = UUID.randomUUID()

    and: "IAM Service is stubbed to return account details for this ID"
      stubFor(get(urlEqualTo("/api/v1/accounts/${userId}"))
          .willReturn(aResponse()
              .withStatus(500)))

    when: "Fetching account endpoint as the authenticated user"
      def result = mockMvc.perform(MockMvcRequestBuilders.get(ACCOUNT_ENDPOINT)
          .with(jwt()
              .authorities(new SimpleGrantedAuthority("ROLE_USER"))
              .jwt { it.subject(userId.toString()) }
          )
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isInternalServerError())
          .andReturn()

    then: "exception message is returned"
      def response = parseJson(result.response.contentAsString)
      response.uuid != null
      response.message != null
      response.type == "EXTERNAL_SERVER_ERROR"
  }

  def "should update current user account details"() {
    given: "An authenticated user ID and update payload"
      def userId = UUID.randomUUID()
      def requestBody = """{
        "firstName": "Jane",
        "lastName": "Smith",
        "avatarUrl": "https://avatar.com/jane"
    }"""

    and: "IAM Service is stubbed to accept the update and return new details"
      stubFor(put(urlEqualTo("/api/v1/accounts/${userId}"))
          .withRequestBody(matchingJsonPath('$.firstName', equalTo("Jane")))
          .withRequestBody(matchingJsonPath('$.lastName', equalTo("Smith")))
          .willReturn(aResponse()
              .withStatus(200)
              .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
              .withBody("""{
                "id": "${userId}",
                "username": "test@example.com",
                "firstName": "Jane",
                "lastName": "Smith",
                "avatarUrl": "https://avatar.com/jane",
                "status": "ACTIVE"
            }""")))

    when: "Performing update request as the authenticated user"
      def result = mockMvc.perform(MockMvcRequestBuilders.put(ACCOUNT_ENDPOINT)
          .with(jwt()
              .authorities(new SimpleGrantedAuthority("ROLE_USER"))
              .jwt { it.subject(userId.toString()) }
          )
          .contentType(MediaType.APPLICATION_JSON)
          .content(requestBody))
          .andExpect(status().isOk())
          .andReturn()

    then: "Updated account details are returned"
      def response = parseJson(result.response.contentAsString)
      response.firstName == "Jane"
      response.lastName == "Smith"
      response.avatarUrl == "https://avatar.com/jane"
  }

  def "should deactivate current user account"() {
    given: "An authenticated user ID"
      def userId = UUID.randomUUID()

    and: "IAM Service is stubbed to handle deletion"
      stubFor(delete(urlEqualTo("/api/v1/accounts/${userId}"))
          .willReturn(aResponse()
              .withStatus(204)))

    when: "Performing deactivate request"
      mockMvc.perform(MockMvcRequestBuilders.delete(ACCOUNT_ENDPOINT)
          .with(jwt()
              .authorities(new SimpleGrantedAuthority("ROLE_USER"))
              .jwt { it.subject(userId.toString()) }
          ))
          .andExpect(status().isOk())

    then: "No exception is thrown and flow completes successfully"
      true
  }

  def "should reject unauthenticated access to secured endpoints"() {
    when: "Requesting account details without JWT"
      mockMvc.perform(MockMvcRequestBuilders.get(ACCOUNT_ENDPOINT))
          .andExpect(status().isUnauthorized())

    then:
      true
  }

  // --- Helper ---

  private Object parseJson(String body) {
    return new JsonSlurper().parseText(body)
  }
}
