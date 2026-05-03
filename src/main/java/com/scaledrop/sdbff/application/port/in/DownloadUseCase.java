package com.scaledrop.sdbff.application.port.in;

import java.util.UUID;

import com.scaledrop.sdbff.domain.download.DownloadObject;

public interface DownloadUseCase {
  DownloadObject getDownloadObject(UUID fileId);
}