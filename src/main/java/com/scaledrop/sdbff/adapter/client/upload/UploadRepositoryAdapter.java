package com.scaledrop.sdbff.adapter.client.upload;

import com.scaledrop.sdbff.adapter.api.model.upload.request.RegisterUploadRequest;
import com.scaledrop.sdbff.adapter.api.model.upload.response.RegisterUploadResponse;
import com.scaledrop.sdbff.application.port.out.UploadRepository;
import com.scaledrop.sdbff.domain.upload.UploadObject;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UploadRepositoryAdapter implements UploadRepository {

  private final UploadClient uploadClient;

  @Override
  public RegisterUploadResponse registerUpload(UploadObject uploadObject) {
    log.info(
        "[UPLOAD-ADAPTER] Forwarding registration for {}: {} (owner: {})",
        uploadObject.getType(),
        uploadObject.getName(),
        uploadObject.getOwnerId());

    RegisterUploadRequest request =
        RegisterUploadRequest.builder()
            .name(uploadObject.getName())
            .location(uploadObject.getLocation())
            .contentType(uploadObject.getContentType())
            .size(uploadObject.getSize())
            .hash(uploadObject.getHash())
            .type(uploadObject.getType())
            .build();

    RegisterUploadResponse response =
        uploadClient.registerUpload(uploadObject.getOwnerId(), request);

    log.info(
        "[UPLOAD-ADAPTER] Successfully registered {}. File ID: {}",
        uploadObject.getType(),
        response.getFileId());

    return response;
  }

  @Override
  public void confirmUpload(UUID fileId) {
    log.info("[UPLOAD-ADAPTER] Confirming physical upload for file ID: {}", fileId);

    uploadClient.confirmUpload(fileId);

    log.info("[UPLOAD-ADAPTER] Confirmation sent successfully for file ID: {}", fileId);
  }
}
