package com.scaledrop.sdbff.configuration.exception;

public class SdBffServiceException extends RuntimeException {

  public SdBffServiceException(String message) {
    super(message);
  }

  public SdBffServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
