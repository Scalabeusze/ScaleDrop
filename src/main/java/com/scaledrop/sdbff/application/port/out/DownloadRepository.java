package com.scaledrop.sdbff.application.port.out;

import java.util.UUID;

import com.scaledrop.sdbff.domain.download.DownloadObject;

public interface DownloadRepository {
  DownloadObject getDownloadObject(UUID fileId);
}