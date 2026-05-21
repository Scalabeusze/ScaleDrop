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

package com.scaledrop.sdupload.adapter.aws;

import com.scaledrop.sdupload.configuration.aws.s3.AmazonS3Properties;
import com.scaledrop.sdupload.configuration.exception.SdUploadServiceException;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Adapter {

  private final S3Presigner s3Presigner;
  private final S3Client s3Client;
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

  public void deleteFile(String s3Key) {
    String bucketName = amazonS3Properties.getFileserver().getBucket();
    String sanitizedKey = sanitizeKey(s3Key);

    log.info(
        "[S3-ADAPTER] Deleting file from S3 with key: {} from bucket: {}",
        sanitizedKey,
        bucketName);

    try {
      DeleteObjectRequest deleteObjectRequest =
          DeleteObjectRequest.builder().bucket(bucketName).key(sanitizedKey).build();

      s3Client.deleteObject(deleteObjectRequest);
      log.info("[S3-ADAPTER] Successfully deleted file from S3");

    } catch (Exception ex) {
      log.error("[S3-ADAPTER] Failed to delete file from S3 for key: {}", sanitizedKey, ex);
      throw new SdUploadServiceException("Could not delete file from S3", ex);
    }
  }

  private String sanitizeKey(String rawKey) {
    if (rawKey.startsWith("/")) {
      return rawKey.substring(1);
    }
    return rawKey;
  }
}
