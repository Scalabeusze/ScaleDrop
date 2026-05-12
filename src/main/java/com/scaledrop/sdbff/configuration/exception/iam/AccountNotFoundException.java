package com.scaledrop.sdbff.configuration.exception.iam;

import com.scaledrop.sdbff.configuration.exception.SdBffServiceException;

public class AccountNotFoundException extends SdBffServiceException {

  public AccountNotFoundException(String message) {
    super(message);
  }
}
