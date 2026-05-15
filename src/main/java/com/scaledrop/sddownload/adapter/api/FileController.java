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

package com.scaledrop.sddownload.adapter.api;

import static com.scaledrop.sddownload.configuration.Constants.API_V1_PREFIX;
import static com.scaledrop.sddownload.configuration.Constants.BASIC_AUTH;

import com.scaledrop.sddownload.adapter.api.mapper.FileResponseMapper;
import com.scaledrop.sddownload.adapter.api.model.response.FileAPIResponse;
import com.scaledrop.sddownload.application.service.FileService;
import com.scaledrop.sddownload.configuration.annotations.DefaultApiExceptionResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(FileController.FILES_ENDPOINT)
@Tag(name = "Files", description = "Files controller")
public class FileController {

  static final String FILES_ENDPOINT = API_V1_PREFIX + "/files";

  private final FileService fileService;
  private final FileResponseMapper fileResponseMapper;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "List files", description = "Lists files from the database")
  @SecurityRequirement(name = BASIC_AUTH)
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "200", description = "Successfully fetched files")
  @ResponseStatus(HttpStatus.OK)
  public List<FileAPIResponse> listFiles(
      @RequestParam(value = "prefix", required = false) String prefix,
      @RequestParam(value = "ownerId", required = false) UUID ownerId) {
    return fileService.listFiles(prefix, ownerId).stream()
        .map(fileResponseMapper::toResponse)
        .toList();
  }

  @GetMapping(value = "/{fileId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get file", description = "Fetches a file by id")
  @SecurityRequirement(name = BASIC_AUTH)
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "200", description = "Successfully fetched file")
  @ResponseStatus(HttpStatus.OK)
  public FileAPIResponse getFile(@PathVariable UUID fileId) {
    return fileResponseMapper.toResponse(fileService.getFile(fileId));
  }

  @GetMapping(value = "/sync", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Sync files",
      description =
          "Expensive operation that scans the configured S3 bucket and syncs files to the database")
  @SecurityRequirement(name = BASIC_AUTH)
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "200", description = "Successfully synced files")
  @ResponseStatus(HttpStatus.OK)
  public List<FileAPIResponse> syncFiles() {
    return fileService.syncFiles().stream().map(fileResponseMapper::toResponse).toList();
  }
}
