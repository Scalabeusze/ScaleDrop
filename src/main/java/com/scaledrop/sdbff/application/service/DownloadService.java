package com.scaledrop.sdbff.application.service;

import com.scaledrop.sdbff.application.port.in.download.DownloadUseCase;
import com.scaledrop.sdbff.application.port.out.DownloadRepository;
import com.scaledrop.sdbff.domain.download.DownloadObject;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadService implements DownloadUseCase {

  private final DownloadRepository downloadRepository;

  @Override
  public DownloadObject getDownloadObject(UUID fileId) {
    log.warn("[DOWNLOAD-SERVICE] Handling download for fileId: {}", fileId);
    return downloadRepository.getDownloadObject(fileId);
  }
}
