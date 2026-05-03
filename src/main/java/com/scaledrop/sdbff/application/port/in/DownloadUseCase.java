package com.scaledrop.sdbff.application.port.in;

import com.scaledrop.sdbff.domain.download.DownloadObject;
import java.util.UUID;

public interface DownloadUseCase {
  DownloadObject getDownloadObject(UUID fileId);
}
