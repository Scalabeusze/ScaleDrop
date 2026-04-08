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
