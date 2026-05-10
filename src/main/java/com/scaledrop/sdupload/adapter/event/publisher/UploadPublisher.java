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
