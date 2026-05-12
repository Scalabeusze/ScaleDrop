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

package com.scaledrop.sdbff.adapter.api.model.account.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Builder
@ToString
@Validated
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountAPIRequest {

  @Size(max = 250) @Schema(
      requiredMode = RequiredMode.NOT_REQUIRED,
      example = "Ferdynand",
      description = "First name of the account holder")
  private String firstName;

  @Size(max = 250) @Schema(
      requiredMode = RequiredMode.NOT_REQUIRED,
      example = "Kiepski",
      description = "Last name of the account holder")
  private String lastName;

  @Size(max = 1024) @Schema(
      requiredMode = RequiredMode.NOT_REQUIRED,
      example = "https://www.new.example.com",
      description = "Avatar url of the account holder")
  private String avatarUrl;
}
