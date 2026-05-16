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

package com.scaledrop.sddownload.adapter.aws;

import com.scaledrop.sddownload.adapter.db.FileEntity;
import com.scaledrop.sddownload.configuration.aws.s3.AmazonS3Properties;
import java.net.URI;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Component
@RequiredArgsConstructor
public class S3DownloadAdapter {

  private static final Duration DOWNLOAD_URL_EXPIRATION = Duration.ofMinutes(15);

  private final S3Presigner s3Presigner;
  private final AmazonS3Properties amazonS3Properties;

  public URI generatePresignedDownloadUrl(FileEntity fileEntity) {
    GetObjectRequest.Builder objectRequest =
        GetObjectRequest.builder()
            .bucket(amazonS3Properties.getFileserver().getBucket())
            .key(fileEntity.getKey())
            .responseContentDisposition(contentDisposition(fileEntity));

    if (StringUtils.isNotBlank(fileEntity.getContentType())) {
      objectRequest.responseContentType(fileEntity.getContentType());
    }

    GetObjectPresignRequest presignRequest =
        GetObjectPresignRequest.builder()
            .signatureDuration(DOWNLOAD_URL_EXPIRATION)
            .getObjectRequest(objectRequest.build())
            .build();

    return URI.create(s3Presigner.presignGetObject(presignRequest).url().toString());
  }

  public Duration downloadUrlExpiration() {
    return DOWNLOAD_URL_EXPIRATION;
  }

  private String contentDisposition(FileEntity fileEntity) {
    return "attachment; filename=\"" + resolveFileName(fileEntity).replace("\"", "") + "\"";
  }

  private String resolveFileName(FileEntity fileEntity) {
    if (StringUtils.isNotBlank(fileEntity.getName())) {
      return fileEntity.getName();
    }

    String fileName = StringUtils.substringAfterLast(fileEntity.getKey(), "/");
    if (StringUtils.isNotBlank(fileName)) {
      return fileName;
    }
    return fileEntity.getKey();
  }
}
