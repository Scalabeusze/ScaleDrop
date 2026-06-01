package com.scaledrop.sdbff.adapter.client.download;

import com.scaledrop.sdbff.adapter.api.model.download.request.FileShareCreateRequest;
import com.scaledrop.sdbff.adapter.api.model.download.response.FileAPIResponse;
import com.scaledrop.sdbff.adapter.api.model.download.response.FileDownloadAPIResponse;
import com.scaledrop.sdbff.adapter.api.model.download.response.FileShareResponse;
import com.scaledrop.sdbff.application.mapper.DownloadResponseMapper;
import com.scaledrop.sdbff.application.port.out.DownloadRepository;
import com.scaledrop.sdbff.domain.download.FileDownloadHistory;
import com.scaledrop.sdbff.domain.download.FileObject;
import com.scaledrop.sdbff.domain.download.FileShare;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DownloadRepositoryAdapter implements DownloadRepository {

  private final DownloadClient downloadClient;
  private final DownloadResponseMapper downloadResponseMapper;

  @Override
  public List<FileObject> listFiles(String prefix, UUID ownerId, Integer limit, Integer offset) {
    log.info(
        "[DOWNLOAD-ADAPTER] Fetching files list. prefix={}, ownerId={}, limit={}, offset={}",
        prefix,
        ownerId,
        limit,
        offset);

    List<FileAPIResponse> responses = downloadClient.listFiles(prefix, ownerId, limit, offset);
    return responses.stream().map(downloadResponseMapper::toDomain).toList();
  }

  @Override
  public FileObject getFile(UUID fileId) {
    log.info("[DOWNLOAD-ADAPTER] Fetching file details for ID: {}", fileId);

    FileAPIResponse response = downloadClient.getFile(fileId);
    return downloadResponseMapper.toDomain(response);
  }

  @Override
  public String downloadFile(UUID fileId) {
    feign.Response response = downloadClient.getDownloadObjectResponse(fileId);
    if (response.status() == 302) {
      var locationHeaders = response.headers().get("Location");
      if (locationHeaders != null && !locationHeaders.isEmpty()) {
        return locationHeaders.iterator().next();
      }
    }
    throw new RuntimeException("Backend did not return a redirect URL.");
  }

  @Override
  public List<FileDownloadHistory> listFileDownloads(
      UUID fileId, UUID ownerId, Integer limit, Integer offset) {

    log.info(
        "[DOWNLOAD-ADAPTER] Fetching download history. fileId={}, ownerId={}, limit={}, offset={}",
        fileId,
        ownerId,
        limit,
        offset);

    List<FileDownloadAPIResponse> responses =
        downloadClient.listFileDownloads(fileId, ownerId, limit, offset);
    return responses.stream().map(downloadResponseMapper::toDomain).toList();
  }

  @Override
  public List<FileObject> syncFiles() {
    log.info("[DOWNLOAD-ADAPTER] Triggering file synchronization with remote service");

    List<FileAPIResponse> responses = downloadClient.syncFiles();
    return responses.stream().map(downloadResponseMapper::toDomain).toList();
  }

  @Override
  public List<FileShare> listFileShares(UUID fromId, UUID toId, Integer limit, Integer offset) {
    log.info(
        "[DOWNLOAD-ADAPTER] Fetching file shares. fromId={}, toId={}, limit={}, offset={}",
        fromId,
        toId,
        limit,
        offset);

    List<FileShareResponse> responses = downloadClient.listFileShares(fromId, toId, limit, offset);
    return responses.stream().map(downloadResponseMapper::toDomain).toList();
  }

  @Override
  public FileShare createFileShare(UUID fileId, UUID fromId, UUID toId) {
    log.info(
        "[DOWNLOAD-ADAPTER] Creating file share in remote service. fileId={}, fromId={}, toId={}",
        fileId,
        fromId,
        toId);

    FileShareCreateRequest request = new FileShareCreateRequest(fileId, fromId, toId);

    FileShareResponse response = downloadClient.createFileShare(request);
    return downloadResponseMapper.toDomain(response);
  }

  @Override
  public void deleteFileShare(UUID shareId) {
    log.info("[DOWNLOAD-ADAPTER] Deleting file share in remote service. shareId={}", shareId);
    downloadClient.deleteFileShare(shareId);
  }
}
