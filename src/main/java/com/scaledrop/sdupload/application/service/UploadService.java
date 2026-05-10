package com.scaledrop.sdupload.application.service;

import com.scaledrop.sdupload.adapter.api.model.request.RegisterUploadRequest;
import com.scaledrop.sdupload.adapter.aws.S3Adapter;
import com.scaledrop.sdupload.adapter.db.FileEntity;
import com.scaledrop.sdupload.adapter.db.FileRepository;
import com.scaledrop.sdupload.adapter.event.publisher.UploadPublisher;
import com.scaledrop.sdupload.application.port.in.RegisterUploadUseCase;
import com.scaledrop.sdupload.configuration.exception.SdUploadServiceException;
import com.scaledrop.sdupload.domain.upload.UploadObject;
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
        "[UPLOAD-SERVICE] Processing file registration: '{}' for user: {}",
        request.getName(),
        ownerId);

    boolean exists =
        fileRepository.existsByOwnerIdAndHashAndLocationAndName(
            ownerId, request.getHash(), request.getLocation(), request.getName());

    if (exists) {
      log.warn("[UPLOAD-SERVICE] Conflict! File already exists for user: {}", ownerId);
      throw new RuntimeException("File conflict: You already have this file in this location.");
    }

    UUID fileId = UUID.randomUUID();

    FileEntity fileEntity =
        FileEntity.builder()
            .id(fileId)
            .ownerId(ownerId)
            .name(request.getName())
            .location(request.getLocation())
            .type("FILE")
            .contentType(request.getContentType())
            .size(request.getSize())
            .hash(request.getHash())
            .status("PENDING")
            .build();

    fileRepository.save(fileEntity);
    log.info("[UPLOAD-SERVICE] Database entry created with status PENDING. File ID: {}", fileId);

    String uploadUrl =
        s3Adapter.generatePreSignedUploadUrl(
            fileId, request.getContentType(), request.getLocation(), request.getName());

    return UploadObject.builder()
        .fileId(fileId)
        .uploadUrl(uploadUrl)
        .status(fileEntity.getStatus())
        .location(request.getLocation() + request.getName())
        .build();
  }

  @Transactional
  public void confirmUpload(UUID fileId) {
    log.info("[UPLOAD-SERVICE] Confirming physical upload for file ID: {}", fileId);

    FileEntity fileEntity =
        fileRepository
            .findById(fileId)
            .orElseThrow(() -> new SdUploadServiceException("File not found with ID: " + fileId));

    if ("UPLOADED".equals(fileEntity.getStatus())) {
      log.info("[UPLOAD-SERVICE] File ID: {} is already confirmed. Skipping.", fileId);
      return;
    }

    fileEntity.setStatus("UPLOADED");
    fileRepository.save(fileEntity);
    log.info("[UPLOAD-SERVICE] Status updated to UPLOADED for file ID: {}", fileId);

    String fullPath = fileEntity.getLocation() + fileEntity.getName();

    UploadObject uploadObject =
        UploadObject.builder()
            .fileId(fileEntity.getId())
            .location(fullPath)
            .status(fileEntity.getStatus())
            .build();

    uploadPublisher.publishEvent(uploadObject);
  }
}
