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

import static com.scaledrop.sddownload.adapter.event.model.UploadType.FILE;

import com.scaledrop.sddownload.adapter.db.FileEntity;
import com.scaledrop.sddownload.adapter.db.FileRepository;
import com.scaledrop.sddownload.adapter.event.model.FileMetadataEvent;
import com.scaledrop.sddownload.configuration.aws.s3.AmazonS3Properties;
import com.scaledrop.sddownload.configuration.exception.DownloadServiceException;
import com.scaledrop.sddownload.configuration.exception.FileUpdateEventException;
import com.scaledrop.sddownload.domain.file.FileObject;
import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

  private static final int S3_MAX_KEYS = 1000;
  private static final String LIST_FILES_ERROR_MESSAGE = "Could not list files";
  private static final String FILE_NOT_FOUND = "File not found";
  private static final String UPLOADED_STATUS = "UPLOADED";

  private final S3Client s3Client;
  private final AmazonS3Properties amazonS3Properties;
  private final FileRepository fileRepository;
  private final Clock clock;

  @Transactional(readOnly = true)
  public List<FileEntity> listFiles(String prefix, UUID ownerId) {
    if (ownerId != null && StringUtils.isNotBlank(prefix)) {
      return fileRepository.findByOwnerIdAndKeyStartingWithOrderByKeyAsc(ownerId, prefix);
    }
    if (ownerId != null) {
      return fileRepository.findByOwnerIdOrderByKeyAsc(ownerId);
    }
    if (StringUtils.isNotBlank(prefix)) {
      return fileRepository.findByKeyStartingWithOrderByKeyAsc(prefix);
    }
    return fileRepository.findAllByOrderByKeyAsc();
  }

  @Transactional(readOnly = true)
  public FileEntity getFile(UUID fileId) {
    return fileRepository
        .findById(fileId)
        .orElseThrow(() -> new EntityNotFoundException(FILE_NOT_FOUND));
  }

  @Transactional
  public List<FileEntity> syncFiles() {
    List<FileObject> s3Files = listS3Files();
    Map<String, FileEntity> existingFiles =
        fileRepository.findAll().stream()
            .collect(Collectors.toMap(FileEntity::getKey, Function.identity()));
    Set<String> syncedKeys = new HashSet<>();

    for (FileObject s3File : s3Files) {
      syncedKeys.add(s3File.key());
      FileEntity fileEntity =
          existingFiles.getOrDefault(
              s3File.key(), FileEntity.builder().id(UUID.randomUUID()).key(s3File.key()).build());

      fileEntity.setSize(s3File.size());
      fileEntity.setLastModified(s3File.lastModified());
      fileEntity.setETag(s3File.eTag());
      fileRepository.save(fileEntity);
    }

    List<FileEntity> staleFiles =
        existingFiles.values().stream()
            .filter(file -> !syncedKeys.contains(file.getKey()))
            .toList();
    fileRepository.deleteAll(staleFiles);

    return fileRepository.findAllByOrderByKeyAsc();
  }

  @Transactional
  public void upsertFileFromUploadEvent(FileMetadataEvent event) {
    if (event == null) {
      throw new FileUpdateEventException("Invalid file update event: payload is empty");
    }
    validateEventRoutingFields(event);

    if (event.type() != FILE || !UPLOADED_STATUS.equals(event.status())) {
      return;
    }
    validateUploadEvent(event);

    FileEntity fileEntity =
        fileRepository
            .findById(event.fileId())
            .orElseGet(() -> resolveFileEntityForUploadEvent(event));

    updateFileEntityFromUploadEvent(fileEntity, event);
    fileRepository.save(fileEntity);
  }

  private List<FileObject> listS3Files() {
    List<FileObject> files = new ArrayList<>();
    String continuationToken = null;

    try {
      do {
        ListObjectsV2Response response = listObjects(continuationToken);

        for (S3Object s3Object : response.contents()) {
          files.add(
              new FileObject(
                  null,
                  s3Object.key(),
                  s3Object.size(),
                  OffsetDateTime.ofInstant(s3Object.lastModified(), ZoneOffset.UTC),
                  normalizeETag(s3Object.eTag())));
        }

        continuationToken = response.isTruncated() ? response.nextContinuationToken() : null;
      } while (continuationToken != null);
    } catch (S3Exception ex) {
      throw new DownloadServiceException(LIST_FILES_ERROR_MESSAGE, ex);
    }

    return files;
  }

  private ListObjectsV2Response listObjects(String continuationToken) {
    return s3Client.listObjectsV2(
        ListObjectsV2Request.builder()
            .bucket(amazonS3Properties.getFileserver().getBucket())
            .continuationToken(continuationToken)
            .maxKeys(S3_MAX_KEYS)
            .build());
  }

  private String normalizeETag(String eTag) {
    return eTag.replace("\"", "");
  }

  private FileEntity resolveFileEntityForUploadEvent(FileMetadataEvent event) {
    String key = resolveKey(event);
    return fileRepository
        .findByKey(key)
        .map(
            existingFile -> {
              fileRepository.delete(existingFile);
              fileRepository.flush();
              return FileEntity.builder().id(event.fileId()).key(key).build();
            })
        .orElseGet(() -> FileEntity.builder().id(event.fileId()).key(key).build());
  }

  private void updateFileEntityFromUploadEvent(FileEntity fileEntity, FileMetadataEvent event) {
    fileEntity.setKey(resolveKey(event));
    fileEntity.setSize(event.size());
    fileEntity.setLastModified(OffsetDateTime.now(clock));
    fileEntity.setETag(event.hash());
    fileEntity.setOwnerId(event.ownerId());
    fileEntity.setName(event.name());
    fileEntity.setLocation(event.location());
    fileEntity.setContentType(event.contentType());
    fileEntity.setStatus(event.status());
  }

  private void validateUploadEvent(FileMetadataEvent event) {
    List<String> missingFields = new ArrayList<>();
    if (event.fileId() == null) {
      missingFields.add("fileId");
    }
    if (StringUtils.isBlank(event.name())) {
      missingFields.add("name");
    }
    if (event.size() == null) {
      missingFields.add("size");
    }
    if (StringUtils.isBlank(event.hash())) {
      missingFields.add("hash");
    }

    if (!missingFields.isEmpty()) {
      throw new FileUpdateEventException(
          "Invalid uploaded file event, missing required fields: "
              + String.join(", ", missingFields));
    }
  }

  private void validateEventRoutingFields(FileMetadataEvent event) {
    List<String> missingFields = new ArrayList<>();
    if (event.type() == null) {
      missingFields.add("type");
    }
    if (StringUtils.isBlank(event.status())) {
      missingFields.add("status");
    }

    if (!missingFields.isEmpty()) {
      throw new FileUpdateEventException(
          "Invalid file update event, missing required fields: "
              + String.join(", ", missingFields));
    }
  }

  private String resolveKey(FileMetadataEvent event) {
    String key = StringUtils.defaultString(event.location()) + event.name();
    if (key.startsWith("/")) {
      return key.substring(1);
    }
    return key;
  }
}
