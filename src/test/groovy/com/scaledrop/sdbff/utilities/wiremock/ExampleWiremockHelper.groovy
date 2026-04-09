package com.scaledrop.sdbff.utilities.wiremock

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.containing
import static com.github.tomakehurst.wiremock.client.WireMock.get
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import static org.springframework.http.HttpHeaders.CONTENT_TYPE
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import com.scaledrop.sdbff.adapter.client.example.ExampleClient
import org.springframework.boot.test.context.TestComponent
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus

@TestComponent
class ExampleWiremockHelper {

  static def stubGetExampleObject(String responseBody) {
    stubFor(get(urlPathEqualTo(ExampleClient.EXAMPLE_URL))
        .withHeader(HttpHeaders.AUTHORIZATION, containing("Basic "))
        .willReturn(aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .withBody(responseBody)
            .withStatus(HttpStatus.OK.value())))
  }
}
