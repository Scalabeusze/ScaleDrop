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
