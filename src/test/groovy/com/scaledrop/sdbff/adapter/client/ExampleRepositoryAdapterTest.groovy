package com.scaledrop.sdbff.adapter.client

import static com.github.tomakehurst.wiremock.client.WireMock.*

import com.scaledrop.sdbff.WiremockTestBase
import com.scaledrop.sdbff.adapter.client.example.ExampleClient
import com.scaledrop.sdbff.application.port.out.ExampleRepository
import com.scaledrop.sdbff.utilities.wiremock.ExampleWiremockHelper
import org.springframework.beans.factory.annotation.Autowired

class ExampleRepositoryAdapterTest extends WiremockTestBase {

  @Autowired
  private ExampleRepository exampleRepository

  def "should return object from HTTP call"() {
    given:
      String resource = fetchResource("mock/example/example-response.json")
      ExampleWiremockHelper.stubGetExampleObject(resource)

    when:
      def exampleObject = exampleRepository.getExampleObject()

    then:
      exampleObject != null
      exampleObject.exampleField == "WEB_RESPONSE"
  }

  def "should use cache instead of calling HTTP twice"() {
    given:
      String resource = fetchResource("mock/example/example-response.json")
      ExampleWiremockHelper.stubGetExampleObject(resource)

    when: "first call"
      def first = exampleRepository.getExampleObject()

    and: "second call"
      def second = exampleRepository.getExampleObject()

    then: "both results are correct"
      first != null
      second != null
      first == second

    and: "HTTP was called only once"
      verify(1, getRequestedFor(urlPathEqualTo(ExampleClient.EXAMPLE_URL)))
  }
}
