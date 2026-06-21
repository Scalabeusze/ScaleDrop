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

package com.scaledrop.sddownload

import com.fasterxml.jackson.databind.ObjectMapper
import com.scaledrop.sddownload.configuration.security.properties.SecurityProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.web.servlet.MockMvc

@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
class WiremockTestBase extends IntegrationTestBase {

  @Autowired
  protected MockMvc mockMvc

  @Autowired
  protected ObjectMapper objectMapper

  @Autowired
  protected SecurityProperties securityProperties

  protected static String DOCUMENTATION_USERNAME
  protected static String DOCUMENTATION_PASSWORD
  protected static String INTERNAL_USERNAME
  protected static String INTERNAL_PASSWORD

  def setup() {
    DOCUMENTATION_USERNAME = securityProperties.getDocumentation().getUsername()
    DOCUMENTATION_PASSWORD = securityProperties.getDocumentation().getPassword()
    INTERNAL_USERNAME = securityProperties.getInternal().getUsername()
    INTERNAL_PASSWORD = securityProperties.getInternal().getPassword()
  }
}
