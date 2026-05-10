package com.scaledrop.sdupload.application.port.in;

import com.scaledrop.sdupload.adapter.api.model.request.RegisterUploadRequest;
import com.scaledrop.sdupload.domain.upload.UploadObject;
import java.util.UUID;

public interface RegisterUploadUseCase {
  UploadObject registerUpload(UUID ownerId, RegisterUploadRequest request);

  void confirmUpload(UUID fileId);
}
