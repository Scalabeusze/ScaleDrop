package com.scaledrop.sdbff.adapter.client.download;

import com.scaledrop.sdbff.application.port.out.DownloadRepository;
import com.scaledrop.sdbff.domain.download.DownloadObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DownloadRepositoryAdapter implements DownloadRepository {

  private final DownloadClient downloadClient;

  @Override
  public DownloadObject getDownloadObject(UUID fileId) {
    log.info("[DOWNLOAD-ADAPTER] Requesting download link from remote service for ID: {}", fileId);

    DownloadObject remoteResponse = downloadClient.getDownloadObject(fileId);

    return DownloadObject.builder()
        .fileName(remoteResponse.getFileName())
        .contentType(remoteResponse.getContentType())
        .downloadUrl(remoteResponse.getDownloadUrl())
        .build();
  }

}