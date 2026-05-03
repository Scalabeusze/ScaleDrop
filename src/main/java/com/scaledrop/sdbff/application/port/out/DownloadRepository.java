package com.scaledrop.sdbff.application.port.out;

import com.scaledrop.sdbff.domain.download.DownloadObject;
import java.util.UUID;

public interface DownloadRepository {
  DownloadObject getDownloadObject(UUID fileId);
}
