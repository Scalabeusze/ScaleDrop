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

package com.scaledrop.sddownload.configuration

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import com.scaledrop.sddownload.WiremockTestBase

class SecurityConfigurationTest extends WiremockTestBase {

  private static final String WRONG_USER = "wrongUser"
  private static final String WRONG_PASSWORD = "wrongPassword"
  private static final String SWAGGER_PATH = "/swagger-ui/index.html"
  private static final String EXAMPLE_ENDPOINT = "/api/v1/example"

  // ACTUATOR
  def "should be able to access actuator endpoints"() {
    expect:
    mockMvc.perform(get("/actuator/health"))
        .andExpect(status().isOk())
  }

  // DOCUMENTATION
  def 'should not allow access to documentation without credentials'() {
    expect:
    mockMvc.perform(get(SWAGGER_PATH))
        .andExpect(status().isUnauthorized())
  }

  def 'should not allow access to documentation with invalid credentials'() {
    expect:
    mockMvc.perform(get(SWAGGER_PATH)
        .with(httpBasic(WRONG_USER, WRONG_PASSWORD)))
        .andExpect(status().isUnauthorized())
  }

  def 'should allow access to documentation with valid credentials'() {
    expect:
    mockMvc.perform(get(SWAGGER_PATH)
        .with(httpBasic(DOCUMENTATION_USERNAME, DOCUMENTATION_PASSWORD)))
        .andExpect(status().isOk())
  }

  // EXAMPLE
  def 'should not allow access to example endpoint without credentials'() {
    expect:
    mockMvc.perform(get(EXAMPLE_ENDPOINT))
        .andExpect(status().isUnauthorized())
  }

  def 'should not allow access to example endpoint with invalid credentials'() {
    expect:
    mockMvc.perform(get(EXAMPLE_ENDPOINT)
        .with(httpBasic(WRONG_USER, WRONG_PASSWORD)))
        .andExpect(status().isUnauthorized())
  }

  def 'should not allow access to example endpoint with documentation credentials'() {
    expect:
    mockMvc.perform(get(EXAMPLE_ENDPOINT)
        .with(httpBasic(DOCUMENTATION_USERNAME, DOCUMENTATION_PASSWORD)))
        .andExpect(status().isForbidden())
  }

  def 'should allow access to example endpoint with internal credentials'() {
    expect:
    mockMvc.perform(get(EXAMPLE_ENDPOINT)
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isOk())
  }
}
