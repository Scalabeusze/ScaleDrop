package com.scaledrop.sdbff.application.port.out;

import com.scaledrop.sdbff.adapter.api.model.upload.response.RegisterUploadResponse;
import com.scaledrop.sdbff.domain.upload.UploadObject;
import java.util.UUID;

public interface UploadRepository {

  RegisterUploadResponse registerUpload(UploadObject uploadObject);

  void confirmUpload(UUID fileId);

  void deleteUpload(UUID ownerId, UUID fileId);
}
