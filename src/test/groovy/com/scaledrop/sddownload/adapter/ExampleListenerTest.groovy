package com.scaledrop.sddownload.adapter

import com.fasterxml.jackson.databind.ObjectMapper
import com.scaledrop.sddownload.IntegrationTestBase
import com.scaledrop.sddownload.adapter.event.listener.ExampleListener
import com.scaledrop.sddownload.domain.example.ExampleObject
import com.scaledrop.sddownload.utilities.Initializer
import com.scaledrop.sddownload.utilities.LoggerWatcher
import org.springframework.beans.factory.annotation.Autowired
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import spock.util.concurrent.PollingConditions

class ExampleListenerTest extends IntegrationTestBase {

  @Autowired
  SqsAsyncClient sqsAsyncClient

  @Autowired
  ObjectMapper objectMapper

  PollingConditions conditions = new PollingConditions(timeout: 5)

  def "should digest event"() {
    given:
      def obj = ExampleObject.builder().exampleField("test").exampleId(UUID.randomUUID()).build()
      def event = objectMapper.writeValueAsString(obj)
      def logs = LoggerWatcher.logsAppenderFor(ExampleListener.class)

    when:
      sqsAsyncClient.sendMessage { it.queueUrl(Initializer.FILE_UPDATES_QUEUE_URL).messageBody(event) }.join()

    then:
      conditions.eventually {
        def messages = logs.list*.formattedMessage
        assert messages.any { it.contains("[EXAMPLE] Processing example event:") }
      }

    cleanup:
      LoggerWatcher.detach(ExampleListener.class, logs)
  }
}
