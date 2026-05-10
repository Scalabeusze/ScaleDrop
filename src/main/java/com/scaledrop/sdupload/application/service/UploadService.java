package com.scaledrop.sdupload.application.service;

import com.scaledrop.sdupload.adapter.api.model.request.RegisterUploadRequest;
import com.scaledrop.sdupload.adapter.aws.S3Adapter;
import com.scaledrop.sdupload.adapter.db.FileEntity;
import com.scaledrop.sdupload.adapter.db.FileRepository;
import com.scaledrop.sdupload.adapter.event.publisher.UploadPublisher;
import com.scaledrop.sdupload.application.port.in.RegisterUploadUseCase;
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
public class UploadService implements RegisterUploadUseCase {

  private final FileRepository fileRepository;
  private final S3Adapter s3Adapter;
  private final UploadPublisher uploadPublisher;

  @Override
  @Transactional
  public UploadObject registerUpload(UUID ownerId, RegisterUploadRequest request) {
    log.info(
        "[UPLOAD-SERVICE] Processing {} registration: '{}' for user: {}",
        request.getType(),
        request.getName(),
        ownerId);

    String finalName = resolveUniqueName(ownerId, request.getLocation(), request.getName());

    if (!finalName.equals(request.getName())) {
      log.info(
          "[UPLOAD-SERVICE] Name collision detected. Renamed '{}' to '{}'",
          request.getName(),
          finalName);
    }

    UUID fileId = UUID.randomUUID();
    String status = (request.getType() == UploadType.FOLDER) ? "UPLOADED" : "PENDING";
    String uploadUrl = null;

    if (request.getType() == UploadType.FILE) {
      uploadUrl =
          s3Adapter.generatePreSignedUploadUrl(
              fileId, request.getContentType(), request.getLocation(), finalName);
    }

    FileEntity fileEntity =
        FileEntity.builder()
            .id(fileId)
            .ownerId(ownerId)
            .name(finalName)
            .location(request.getLocation())
            .type(request.getType())
            .contentType(request.getContentType())
            .size(request.getSize())
            .hash(request.getHash())
            .status(status)
            .build();

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

  private String resolveUniqueName(UUID ownerId, String location, String originalName) {
    String currentName = originalName;
    int counter = 1;

    int lastDot = originalName.lastIndexOf('.');
    String baseName = (lastDot != -1) ? originalName.substring(0, lastDot) : originalName;
    String extension = (lastDot != -1) ? originalName.substring(lastDot) : "";

    while (fileRepository.existsByOwnerIdAndLocationAndName(ownerId, location, currentName)) {
      currentName = String.format("%s(%d)%s", baseName, counter, extension);
      counter++;
    }
    return currentName;
  }

  private void publishUploadEvent(FileEntity entity) {
    FileMetadata metadata =
        FileMetadata.builder()
            .fileId(entity.getId())
            .ownerId(entity.getOwnerId())
            .name(entity.getName())
            .location(entity.getLocation())
            .contentType(entity.getContentType())
            .size(entity.getSize())
            .hash(entity.getHash())
            .status(entity.getStatus())
            .type(entity.getType())
            .build();

    uploadPublisher.publishEvent(metadata);
  }
}
