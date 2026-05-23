package com.scaledrop.sdbff.configuration.exception.upload;

import com.scaledrop.sdbff.configuration.exception.SdBffServiceException;

public class UploadNotFoundException extends SdBffServiceException {
  public UploadNotFoundException(String message) {
    super(message);
  }
}
