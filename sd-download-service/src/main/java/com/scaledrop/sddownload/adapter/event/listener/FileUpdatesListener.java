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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaledrop.sddownload.adapter.event.model.FileMetadataEvent;
import com.scaledrop.sddownload.application.service.FileService;
import com.scaledrop.sddownload.configuration.exception.FileUpdateEventException;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.annotation.SqsListenerAcknowledgementMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileUpdatesListener {

  private static final String SNS_MESSAGE_FIELD = "Message";

  private final ObjectMapper objectMapper;
  private final FileService fileService;

  @SqsListener(
      value = "${aws.sqs.fileUpdatesQueueUrl}",
      acknowledgementMode = SqsListenerAcknowledgementMode.ON_SUCCESS)
  public void onEvent(String messageBody) {
    FileMetadataEvent event = parseEvent(messageBody);
    fileService.upsertFileFromUploadEvent(event);
  }

  private FileMetadataEvent parseEvent(String messageBody) {
    try {
      JsonNode root = objectMapper.readTree(messageBody);
      JsonNode message = root.get(SNS_MESSAGE_FIELD);
      if (message == null) {
        return objectMapper.treeToValue(root, FileMetadataEvent.class);
      }
      if (message.isTextual()) {
        return objectMapper.readValue(message.asText(), FileMetadataEvent.class);
      }
      return objectMapper.treeToValue(message, FileMetadataEvent.class);
    } catch (Exception ex) {
      throw new FileUpdateEventException("Invalid file update event payload", ex);
    }
  }
}
