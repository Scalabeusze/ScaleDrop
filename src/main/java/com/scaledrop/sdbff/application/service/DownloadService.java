package com.scaledrop.sdbff.application.service;

import com.scaledrop.sdbff.application.port.in.DownloadUseCase;
import com.scaledrop.sdbff.application.port.out.DownloadRepository;
import com.scaledrop.sdbff.domain.download.FileDownloadHistory;
import com.scaledrop.sdbff.domain.download.FileObject;
import java.util.List;
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
  public List<FileObject> listFiles(String prefix, UUID ownerId, Integer limit, Integer offset) {
    log.info("[BFF-SERVICE] Listing files for owner: {}", ownerId);
    return downloadRepository.listFiles(prefix, ownerId, limit, offset);
  }

  @Override
  public FileObject getFile(UUID fileId) {
    log.info("[BFF-SERVICE] Fetching file details: {}", fileId);
    return downloadRepository.getFile(fileId);
  }

  @Override
  public String downloadFile(UUID fileId) {
    log.info("[BFF-SERVICE] Generating download link for file: {}", fileId);
    return downloadRepository.downloadFile(fileId);
  }

  @Override
  public List<FileDownloadHistory> listFileDownloads(
      UUID fileId, UUID ownerId, Integer limit, Integer offset) {
    log.info("[BFF-SERVICE] Listing download history for owner: {}", ownerId);
    return downloadRepository.listFileDownloads(fileId, ownerId, limit, offset);
  }

  @Override
  public List<FileObject> syncFiles() {
    log.info("[BFF-SERVICE] Triggering manual file synchronization");
    return downloadRepository.syncFiles();
  }
}
