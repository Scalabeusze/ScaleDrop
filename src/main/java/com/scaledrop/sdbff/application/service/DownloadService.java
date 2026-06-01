package com.scaledrop.sdbff.application.service;

import com.scaledrop.sdbff.application.port.in.DownloadUseCase;
import com.scaledrop.sdbff.application.port.out.DownloadRepository;
import com.scaledrop.sdbff.domain.download.FileDownloadHistory;
import com.scaledrop.sdbff.domain.download.FileObject;
import com.scaledrop.sdbff.domain.download.FileShare;
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

  @Override
  public List<FileShare> listFileShares(UUID fromId, UUID toId, Integer limit, Integer offset) {
    log.info(
        "[BFF-SERVICE] Listing file shares. fromId: {}, toId: {}, limit: {}, offset: {}",
        fromId,
        toId,
        limit,
        offset);
    return downloadRepository.listFileShares(fromId, toId, limit, offset);
  }

  @Override
  public FileShare createFileShare(UUID fileId, UUID fromId, UUID toId) {
    log.info(
        "[BFF-SERVICE] Creating file share for fileId: {} from user: {} to user: {}",
        fileId,
        fromId,
        toId);
    return downloadRepository.createFileShare(fileId, fromId, toId);
  }

  @Override
  public void deleteFileShare(UUID shareId) {
    log.info("[BFF-SERVICE] Deleting file share with ID: {}", shareId);
    downloadRepository.deleteFileShare(shareId);
  }
}
