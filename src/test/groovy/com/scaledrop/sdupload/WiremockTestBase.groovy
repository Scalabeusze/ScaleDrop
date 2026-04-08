package com.scaledrop.sdupload

import com.fasterxml.jackson.databind.ObjectMapper
import com.scaledrop.sdupload.configuration.security.properties.SecurityProperties
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
