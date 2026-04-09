/*
 * Copyright 2026-present Scalabeusze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.scaledrop.sdaccount.configuration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {

  public static final String BASIC_AUTH = "basic-auth";

  public static final String API_V1_PREFIX = "/api/v1";

  public static final String INVALID_REQUEST_VALIDATION_FAILED =
      "Invalid request, validation failed";
  public static final String INVALID_AUTHORIZATION = "Invalid basic authorization";
  public static final String NOT_FOUND = "Resource not found";
}
