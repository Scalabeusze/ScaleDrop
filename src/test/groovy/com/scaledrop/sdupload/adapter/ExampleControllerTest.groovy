package com.scaledrop.sdupload.adapter

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import com.scaledrop.sdupload.WiremockTestBase
import com.scaledrop.sdupload.adapter.api.model.response.ExampleAPIResponse
import com.scaledrop.sdupload.domain.example.ExampleObject
import com.scaledrop.sdupload.utilities.sns.SnsClientDecorator
import org.springframework.beans.factory.annotation.Autowired
import spock.util.concurrent.PollingConditions

class ExampleControllerTest extends WiremockTestBase {

  private static final String EXAMPLE_ENDPOINT = "/api/v1/example"

  @Autowired
  private SnsClientDecorator snsClientDecorator

  PollingConditions conditions = new PollingConditions(timeout: 5)

  def 'should publish event to SNS'() {
    when:
      def result = mockMvc.perform(get(EXAMPLE_ENDPOINT)
          .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
          .andExpect(status().isOk())
          .andReturn()

    then:
      def response = objectMapper.readValue(result.getResponse().getContentAsString(), ExampleAPIResponse.class)

      conditions.eventually {
        def messages = snsClientDecorator.getPublishedRequests()
        assert messages.size() == 1
        def messageContent = objectMapper.readValue(messages[0].message(), ExampleObject.class)
        assert messageContent.getExampleField() == response.getExampleField()
      }
  }
}
