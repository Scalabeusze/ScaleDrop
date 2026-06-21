package com.scaledrop.sdbff.configuration.exception.upload;

import com.scaledrop.sdbff.configuration.exception.SdBffServiceException;

public class UploadValidationException extends SdBffServiceException {
  public UploadValidationException(String message) {
    super(message);
  }
}
