package com.scaledrop.sdbff.application.port.in.upload;

import com.scaledrop.sdbff.domain.upload.UploadObject;

public interface UploadUseCase {
  String getUploadUrl(UploadObject upload);
}
