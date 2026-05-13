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

package com.scaledrop.sddownload.adapter.event.listener;

import com.gruelbox.transactionoutbox.TransactionOutbox;
import com.scaledrop.sddownload.domain.example.ExampleObject;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.annotation.SqsListenerAcknowledgementMode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExampleListener {

  private final TransactionOutbox transactionOutbox;

  @Transactional
  @SqsListener(
      value = "${aws.sqs.fileUpdatesQueueUrl}",
      acknowledgementMode = SqsListenerAcknowledgementMode.ON_SUCCESS)
  public void onEvent(ExampleObject event) {
    transactionOutbox.schedule(this.getClass()).processEvent(event);
  }

  public void processEvent(ExampleObject event) {
    log.info("[EXAMPLE] Processing example event: {}", event);
  }
}
