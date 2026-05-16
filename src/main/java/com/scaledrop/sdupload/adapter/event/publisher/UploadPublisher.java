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

package com.scaledrop.sdupload.adapter.event.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gruelbox.transactionoutbox.TransactionOutbox;
import com.scaledrop.sdupload.configuration.aws.sns.AmazonSnsProperties;
import com.scaledrop.sdupload.configuration.exception.SdUploadServiceException;
import com.scaledrop.sdupload.domain.upload.FileMetadata;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadPublisher {

  private final SnsClient snsClient;
  private final ObjectMapper objectMapper;
  private final TransactionOutbox transactionOutbox;
  private final AmazonSnsProperties amazonSnsProperties;

  @Transactional
  public void publishEvent(FileMetadata fileMetadata) {
    transactionOutbox.schedule(this.getClass()).sendEventToTopic(fileMetadata);
  }

  void sendEventToTopic(FileMetadata eventPayload) {
    try {
      String message = objectMapper.writeValueAsString(eventPayload);
      PublishRequest publishRequest =
          PublishRequest.builder()
              .topicArn(amazonSnsProperties.getFileUpdatesTopicArn())
              .message(message)
              .build();
      snsClient.publish(publishRequest);
      log.info("[UPLOAD-PUBLISHER] Published event to SNS topic: {}", eventPayload);
    } catch (Exception ex) {
      log.error(
          "[UPLOAD-PUBLISHER] Failed to publish event to SNS topic: {}, error: {}",
          eventPayload,
          ex.getMessage(),
          ex);
      throw new SdUploadServiceException("Failed to publish event to SNS topic", ex);
    }
  }
}
