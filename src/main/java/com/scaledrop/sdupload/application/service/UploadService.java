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

package com.scaledrop.sdupload.application.service;

import com.scaledrop.sdupload.adapter.api.model.request.UploadRequest;
import com.scaledrop.sdupload.adapter.aws.S3Adapter;
import com.scaledrop.sdupload.adapter.db.FileEntity;
import com.scaledrop.sdupload.adapter.db.FileRepository;
import com.scaledrop.sdupload.adapter.event.publisher.UploadPublisher;
import com.scaledrop.sdupload.application.mapper.FileEntityMapper;
import com.scaledrop.sdupload.application.mapper.FileMetadataMapper;
import com.scaledrop.sdupload.application.port.in.UploadUseCase;
import com.scaledrop.sdupload.configuration.exception.SdUploadServiceException;
import com.scaledrop.sdupload.domain.upload.FileMetadata;
import com.scaledrop.sdupload.domain.upload.UploadObject;
import com.scaledrop.sdupload.domain.upload.UploadType;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadService implements UploadUseCase {

  private final FileRepository fileRepository;
  private final S3Adapter s3Adapter;
  private final UploadPublisher uploadPublisher;
  private final FileEntityMapper fileEntityMapper;
  private final FileMetadataMapper fileMetadataMapper;

  @Override
  @Transactional
  public UploadObject registerUpload(UUID ownerId, UploadRequest request) {
    log.info(
        "[UPLOAD-SERVICE] Processing {} registration: '{}' for user: {}",
        request.getType(),
        request.getName(),
        ownerId);

    UUID fileId = UUID.randomUUID();
    String status = (request.getType() == UploadType.FOLDER) ? "UPLOADED" : "PENDING";
    String uploadUrl = null;

    if (request.getType() == UploadType.FILE) {
      String s3Key = ownerId.toString() + "/" + fileId.toString();
      uploadUrl = s3Adapter.generatePreSignedUploadUrl(fileId, request.getContentType(), "", s3Key);
    }

    FileEntity fileEntity = fileEntityMapper.toEntity(request, fileId, ownerId, status);

    fileRepository.save(fileEntity);
    log.info(
        "[UPLOAD-SERVICE] {} saved to DB with status {}. ID: {}",
        request.getType(),
        status,
        fileId);

    if (request.getType() == UploadType.FOLDER) {
      publishUploadEvent(fileEntity);
    }

    return UploadObject.builder()
        .fileId(fileId)
        .location(fileEntity.getLocation() + fileEntity.getName())
        .uploadUrl(uploadUrl)
        .status(fileEntity.getStatus())
        .type(fileEntity.getType())
        .build();
  }

  @Override
  @Transactional
  public void confirmUpload(UUID fileId) {
    log.info("[UPLOAD-SERVICE] Confirming physical upload for ID: {}", fileId);

    FileEntity fileEntity =
        fileRepository
            .findById(fileId)
            .orElseThrow(() -> new SdUploadServiceException("Object not found: " + fileId));

    if ("UPLOADED".equals(fileEntity.getStatus())) {
      return;
    }

    fileEntity.setStatus("UPLOADED");
    fileRepository.save(fileEntity);

    publishUploadEvent(fileEntity);
  }

  @Transactional
  public void deleteUpload(UUID ownerId, UUID fileId) {
    log.info("[UPLOAD-SERVICE] User {} requested deletion of file {}", ownerId, fileId);

    FileEntity fileEntity =
        fileRepository
            .findById(fileId)
            .orElseThrow(() -> new SdUploadServiceException("Object not found: " + fileId));

    if (!fileEntity.getOwnerId().equals(ownerId)) {
      log.warn(
          "Security alert! User {} tried to delete file {} belonging to {}",
          ownerId,
          fileId,
          fileEntity.getOwnerId());
      throw new SdUploadServiceException("You do not have permission to delete this file");
    }

    if (fileEntity.getType() == UploadType.FILE) {
      String s3Key = ownerId.toString() + "/" + fileId.toString();
      s3Adapter.deleteFile(s3Key);
    }

    fileRepository.delete(fileEntity);

    fileEntity.setStatus("DELETED");
    publishUploadEvent(fileEntity);

    log.info("[UPLOAD-SERVICE] Successfully deleted file {}", fileId);
  }

  private void publishUploadEvent(FileEntity entity) {

    FileMetadata metadata = fileMetadataMapper.toMetadata(entity);
    uploadPublisher.publishEvent(metadata);
  }
}
