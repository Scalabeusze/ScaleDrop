package com.scaledrop.sdupload.adapter.aws;

import com.scaledrop.sdupload.configuration.aws.s3.AmazonS3Properties;
import com.scaledrop.sdupload.configuration.exception.SdUploadServiceException;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Adapter {

  private final S3Presigner s3Presigner;
  private final AmazonS3Properties amazonS3Properties;

  public String generatePreSignedUploadUrl(
      UUID fileId, String contentType, String location, String fileName) {
    String s3Key = sanitizeKey(location + fileName);
    String bucketName = amazonS3Properties.getFileserver().getBucket();

    log.info(
        "[S3-ADAPTER] Generating pre-signed URL for Bucket: '{}', Key: '{}'", bucketName, s3Key);

    try {
      PutObjectRequest objectRequest =
          PutObjectRequest.builder().bucket(bucketName).key(s3Key).contentType(contentType).build();

      PutObjectPresignRequest presignRequest =
          PutObjectPresignRequest.builder()
              .signatureDuration(Duration.ofMinutes(15))
              .putObjectRequest(objectRequest)
              .build();

      PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
      String url = presignedRequest.url().toString();

      log.info("[S3-ADAPTER] Successfully generated pre-signed URL for fileId: {}", fileId);
      return url;

    } catch (Exception ex) {
      log.error("[S3-ADAPTER] Failed to generate pre-signed URL for key: {}", s3Key, ex);
      throw new SdUploadServiceException("Could not generate S3 upload link", ex);
    }
  }

  private String sanitizeKey(String rawKey) {
    if (rawKey.startsWith("/")) {
      return rawKey.substring(1);
    }
    return rawKey;
  }
}
