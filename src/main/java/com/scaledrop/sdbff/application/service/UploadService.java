package com.scaledrop.sdbff.application.service;

import com.scaledrop.sdbff.application.port.in.UploadUseCase;
import com.scaledrop.sdbff.application.port.out.UploadRepository;
import com.scaledrop.sdbff.domain.upload.UploadObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadService implements UploadUseCase {

  private final UploadRepository uploadRepository;

  @Override
  public String getUploadUrl(UploadObject uploadObject) {
    log.warn("[UPLOAD-SERVICE] Handling upload for hash: {}", uploadObject.getHash());
    return uploadRepository.getUploadUrl(uploadObject);
  }
}
