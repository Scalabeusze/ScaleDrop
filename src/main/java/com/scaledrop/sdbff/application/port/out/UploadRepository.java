package com.scaledrop.sdbff.application.port.out;

import com.scaledrop.sdbff.domain.upload.UploadObject;

public interface UploadRepository {
    String getUploadUrl(UploadObject uploadObject);
}