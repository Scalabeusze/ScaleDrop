package com.scaledrop.sdbff.configuration.exception.iam;

import com.scaledrop.sdbff.configuration.exception.SdBffServiceException;

public class LoginException extends SdBffServiceException {

  public LoginException(String message) {
    super(message);
  }
}
