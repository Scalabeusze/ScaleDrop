package com.scaledrop.sddownload

import static java.nio.charset.StandardCharsets.UTF_8
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import com.fasterxml.jackson.databind.ObjectMapper
import com.scaledrop.sddownload.utilities.Initializer

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import wiremock.com.google.common.io.Resources

@AutoConfigureWireMock(port = 0)
@AutoConfigureObservability
@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(initializers = [Initializer])
class IntegrationTestBase extends Specification {

  @Autowired
  ObjectMapper objectMapper

  static String fetchResource(String resourcePath) throws IOException {
    return Resources.toString(Resources.getResource(resourcePath), UTF_8)
  }
}
