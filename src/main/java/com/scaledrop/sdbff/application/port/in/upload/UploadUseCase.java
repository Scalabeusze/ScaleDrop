package com.scaledrop.sdbff.application.port.in.upload;

import com.scaledrop.sdbff.adapter.api.model.upload.response.RegisterUploadResponse;
import com.scaledrop.sdbff.domain.upload.UploadObject;
import java.util.UUID;

public interface UploadUseCase {
  RegisterUploadResponse registerUpload(UploadObject uploadObject);

  void confirmUpload(UUID fileId);
}
