package com.scaledrop.sdbff.configuration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {

  public static final String BASIC_AUTH = "basic-auth";
  public static final String BEARER_JWT = "bearer-jwt";

  public static final String API_V1_PREFIX = "/api/v1";

  public static final String INVALID_REQUEST_VALIDATION_FAILED =
      "Invalid request, validation failed";
  public static final String INVALID_AUTHORIZATION = "Invalid basic authorization";
  public static final String NOT_FOUND = "Resource not found";
}
