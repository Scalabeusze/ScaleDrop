package com.scaledrop.sddownload.adapter

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import com.scaledrop.sddownload.WiremockTestBase

class ExampleControllerTest extends WiremockTestBase {

  private static final String EXAMPLE_ENDPOINT = "/api/v1/example"

  def 'should return 200'() {
    expect:
      mockMvc.perform(get(EXAMPLE_ENDPOINT)
          .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
          .andExpect(status().isOk())
  }
}
