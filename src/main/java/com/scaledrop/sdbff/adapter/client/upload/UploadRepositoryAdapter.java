package com.scaledrop.sdbff.adapter.client.upload;

import com.scaledrop.sdbff.application.port.out.UploadRepository;
import com.scaledrop.sdbff.domain.upload.UploadObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UploadRepositoryAdapter implements UploadRepository {

  private final UploadClient uploadClient;

  @Override
  public String getUploadUrl(UploadObject metadata) {
    log.info("[UPLOAD-ADAPTER] Requesting pre-signed URL for file: {} (owner: {})",
        metadata.getFileName(), metadata.getOwnerId());

    String preSignedUrl = uploadClient.getUploadUrl(metadata);

    log.info("[UPLOAD-ADAPTER] Successfully retrieved pre-signed URL for file: {}", metadata.getFileName());

    return preSignedUrl;
  }
}