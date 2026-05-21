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

package com.scaledrop.sdupload.adapter.api;

import static com.scaledrop.sdupload.configuration.Constants.API_V1_PREFIX;

import com.scaledrop.sdupload.adapter.api.mapper.UploadResponseMapper;
import com.scaledrop.sdupload.adapter.api.model.request.UploadRequest;
import com.scaledrop.sdupload.adapter.api.model.response.UploadResponse;
import com.scaledrop.sdupload.application.port.in.UploadUseCase;
import com.scaledrop.sdupload.configuration.annotations.DefaultApiExceptionResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "Upload", description = "File upload and metadata management")
public class UploadController {

  private static final String UPLOAD_ENDPOINT = API_V1_PREFIX + "/upload";

  private final UploadUseCase registerUploadUseCase;
  private final UploadResponseMapper uploadResponseMapper;

  @PostMapping(
      value = UPLOAD_ENDPOINT,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Register file upload",
      description = "Registers file metadata and returns a generated upload URL with location")
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "201", description = "Successfully registered upload metadata")
  @ResponseStatus(HttpStatus.CREATED)
  public UploadResponse registerUpload(
      @RequestHeader("X-User-Id") UUID ownerId, @RequestBody @Validated UploadRequest request) {

    log.info(
        "[UPLOAD-CONTROLLER] Received upload registration request for user: {} file: {}",
        ownerId,
        request.getName());

    return uploadResponseMapper.toResponse(registerUploadUseCase.registerUpload(ownerId, request));
  }

  @PostMapping(value = UPLOAD_ENDPOINT + "/{fileId}/confirm")
  @Operation(
      summary = "Confirm physical file upload",
      description = "Updates status to UPLOADED and triggers SNS notification via Outbox")
  @ResponseStatus(HttpStatus.OK)
  public void confirmUpload(@PathVariable("fileId") UUID fileId) {

    log.info("[UPLOAD-CONTROLLER] Received confirmation trigger for file ID: {}", fileId);
    registerUploadUseCase.confirmUpload(fileId);
  }

  @DeleteMapping(value = UPLOAD_ENDPOINT + "/{fileId}")
  @Operation(
      summary = "Delete file or folder",
      description = "Deletes file metadata from DB and physically removes it from S3")
  @ApiResponse(responseCode = "204", description = "Successfully deleted file or folder")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUpload(
      @RequestHeader("X-User-Id") UUID ownerId, @PathVariable("fileId") UUID fileId) {

    log.info(
        "[UPLOAD-CONTROLLER] Received delete request from user: {} for file ID: {}",
        ownerId,
        fileId);
    registerUploadUseCase.deleteUpload(ownerId, fileId);
  }
}
