package com.scaledrop.sdbff.configuration.exception.download;

public class DownloadNotFoundException extends RuntimeException {
  public DownloadNotFoundException(String message) {
    super(message);
  }
}
