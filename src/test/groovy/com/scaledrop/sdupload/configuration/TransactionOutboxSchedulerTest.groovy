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

package com.scaledrop.sdupload.configuration

import com.gruelbox.transactionoutbox.TransactionOutbox
import com.scaledrop.sdupload.configuration.transactionoutbox.TransactionOutboxScheduler
import spock.lang.Specification

class TransactionOutboxSchedulerTest extends Specification {

  private TransactionOutbox transactionOutbox = Mock()
  private TransactionOutboxScheduler scheduler = new TransactionOutboxScheduler(transactionOutbox)

  def setup() {
    // Ensure test thread is not left interrupted between tests.
    Thread.interrupted()
  }

  def "should flush until outbox is empty"() {
    when:
    scheduler.transactionOutboxScheduledTask()

    then:
    1 * transactionOutbox.flush() >> true
    1 * transactionOutbox.flush() >> true
    1 * transactionOutbox.flush() >> false
    0 * _
  }

  def "should catch exception thrown during flush"() {
    when:
    scheduler.transactionOutboxScheduledTask()

    then:
    1 * transactionOutbox.flush() >> { throw new RuntimeException("flush failed") }
    notThrown(Exception)
  }
}
