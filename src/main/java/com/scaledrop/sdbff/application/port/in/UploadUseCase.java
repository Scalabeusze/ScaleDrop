package com.scaledrop.sdbff.application.port.in;

import com.scaledrop.sdbff.domain.upload.UploadObject;

public interface UploadUseCase {
  String getUploadUrl(UploadObject upload);
}
