package com.scaledrop.sdupload.configuration.transactionoutbox;

import com.gruelbox.transactionoutbox.TransactionOutbox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.scheduling.publish-event.enabled", havingValue = "true")
@RequiredArgsConstructor
public class TransactionOutboxScheduler {

  private final TransactionOutbox transactionOutbox;

  @Scheduled(fixedDelayString = "${app.scheduling.publish-event.fixed-delay}", initialDelayString = "10s")
  public void transactionOutboxScheduledTask() {
    if (!Thread.interrupted()) {
      try {
        // Keep flushing work until there's nothing left to flush
        while (transactionOutbox.flush()) {
          log.info("[TransactionOutbox] - Flushing the message");
        }
      } catch (Exception e) {
        log.error("[TransactionOutbox] - Error flushing transaction inbox", e);
      }
    }
  }
}
