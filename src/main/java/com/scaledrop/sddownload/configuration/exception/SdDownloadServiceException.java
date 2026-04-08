package com.scaledrop.sddownload.configuration.exception;

public class SdDownloadServiceException extends RuntimeException {

  public SdDownloadServiceException(String message) {
    super(message);
  }

  public SdDownloadServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
