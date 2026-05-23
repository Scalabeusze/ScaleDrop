package com.scaledrop.sdbff.application.port.in;

import com.scaledrop.sdbff.domain.download.FileDownloadHistory;
import com.scaledrop.sdbff.domain.download.FileObject;
import java.util.List;
import java.util.UUID;

public interface DownloadUseCase {
  List<FileObject> listFiles(String prefix, UUID ownerId, Integer limit, Integer offset);

  FileObject getFile(UUID fileId);

  String downloadFile(UUID fileId);

  List<FileObject> syncFiles();

  List<FileDownloadHistory> listFileDownloads(
      UUID fileId, UUID ownerId, Integer limit, Integer offset);
}
