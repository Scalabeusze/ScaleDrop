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

  @Scheduled(
      fixedDelayString = "${app.scheduling.publish-event.fixed-delay}",
      initialDelayString = "10s")
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
