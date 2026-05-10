package com.scaledrop.sdbff.application.service;

import com.scaledrop.sdbff.adapter.api.model.upload.response.RegisterUploadResponse;
import com.scaledrop.sdbff.application.port.in.upload.UploadUseCase;
import com.scaledrop.sdbff.application.port.out.UploadRepository;
import com.scaledrop.sdbff.domain.upload.UploadObject;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadService implements UploadUseCase {

  private final UploadRepository uploadRepository;

  @Override
  public RegisterUploadResponse registerUpload(UploadObject uploadObject) {
    log.info(
        "[UPLOAD-SERVICE] Initiating {} registration for user: {}, name: {}",
        uploadObject.getType(),
        uploadObject.getOwnerId(),
        uploadObject.getName());

    return uploadRepository.registerUpload(uploadObject);
  }

  @Override
  public void confirmUpload(UUID fileId) {
    log.info("[UPLOAD-SERVICE] Confirming upload for fileId: {}", fileId);

    uploadRepository.confirmUpload(fileId);
  }
}
