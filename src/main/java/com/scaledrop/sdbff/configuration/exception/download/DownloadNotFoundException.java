package com.scaledrop.sdbff.configuration.exception.download;

import com.scaledrop.sdbff.configuration.exception.SdBffServiceException;

public class DownloadNotFoundException extends SdBffServiceException {
  public DownloadNotFoundException(String message) {
    super(message);
  }
}
