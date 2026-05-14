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

package com.scaledrop.sddownload.application.service;

import com.scaledrop.sddownload.configuration.aws.s3.AmazonS3Properties;
import com.scaledrop.sddownload.configuration.exception.DownloadServiceException;
import com.scaledrop.sddownload.domain.file.FileObject;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
@RequiredArgsConstructor
public class FileService {

  private static final int S3_MAX_KEYS = 1000;
  private static final String LIST_FILES_ERROR_MESSAGE = "Could not list files";

  private final S3Client s3Client;
  private final AmazonS3Properties amazonS3Properties;

  public List<FileObject> listFiles(String prefix) {
    List<FileObject> files = new ArrayList<>();
    String continuationToken = null;

    try {
      do {
        ListObjectsV2Response response = listObjects(prefix, continuationToken);

        for (S3Object s3Object : response.contents()) {
          files.add(
              new FileObject(
                  s3Object.key(),
                  s3Object.size(),
                  s3Object.lastModified(),
                  s3Object.eTag().replaceAll("\"", "")));
        }

        continuationToken = response.isTruncated() ? response.nextContinuationToken() : null;
      } while (continuationToken != null);
    } catch (S3Exception ex) {
      throw new DownloadServiceException(LIST_FILES_ERROR_MESSAGE, ex);
    }

    return files;
  }

  private ListObjectsV2Response listObjects(String prefix, String continuationToken) {
    return s3Client.listObjectsV2(
        ListObjectsV2Request.builder()
            .bucket(amazonS3Properties.getFileserver().getBucket())
            .prefix(prefix)
            .continuationToken(continuationToken)
            .maxKeys(S3_MAX_KEYS)
            .build());
  }
}
