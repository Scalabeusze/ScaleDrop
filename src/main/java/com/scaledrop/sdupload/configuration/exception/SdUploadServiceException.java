package com.scaledrop.sdupload.configuration.exception;

public class SdUploadServiceException extends RuntimeException {

  public SdUploadServiceException(String message) {
    super(message);
  }

  public SdUploadServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
