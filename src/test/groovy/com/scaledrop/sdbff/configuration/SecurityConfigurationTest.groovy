package com.scaledrop.sdbff.configuration

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import com.scaledrop.sdbff.WiremockTestBase
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.security.core.authority.SimpleGrantedAuthority

class SecurityConfigurationTest extends WiremockTestBase {

  private static final String WRONG_USER = "wrongUser"
  private static final String WRONG_PASSWORD = "wrongPassword"
  private static final String SWAGGER_PATH = "/swagger-ui/index.html"

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
}
