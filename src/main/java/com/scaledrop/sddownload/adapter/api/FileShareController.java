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

import com.scaledrop.sddownload.adapter.api.mapper.FileShareResponseMapper;
import com.scaledrop.sddownload.adapter.api.model.request.FileShareCreateAPIRequest;
import com.scaledrop.sddownload.adapter.api.model.response.FileShareAPIResponse;
import com.scaledrop.sddownload.application.service.FileService;
import com.scaledrop.sddownload.configuration.annotations.DefaultApiExceptionResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(FileShareController.FILE_SHARES_ENDPOINT)
@Tag(name = "File shares", description = "File shares controller")
public class FileShareController {

  static final String FILE_SHARES_ENDPOINT = API_V1_PREFIX + "/file-shares";

  private final FileService fileService;
  private final FileShareResponseMapper fileShareResponseMapper;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "List file shares", description = "Lists file shares")
  @SecurityRequirement(name = BASIC_AUTH)
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "200", description = "Successfully fetched file shares")
  @ResponseStatus(HttpStatus.OK)
  public List<FileShareAPIResponse> listFileShares(
      @RequestParam(value = "fromId", required = false) UUID fromId,
      @RequestParam(value = "toId", required = false) UUID toId,
      @RequestParam(value = "limit", required = true, defaultValue = "1000") @Min(0) @Max(1000) Integer limit,
      @RequestParam(value = "offset", required = true, defaultValue = "0") @Min(0) Integer offset) {
    return fileService.listFileShares(fromId, toId, limit, offset).stream()
        .map(fileShareResponseMapper::toResponse)
        .toList();
  }

  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Create file share", description = "Creates a file share")
  @SecurityRequirement(name = BASIC_AUTH)
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "201", description = "Successfully created file share")
  @ResponseStatus(HttpStatus.CREATED)
  public FileShareAPIResponse createFileShare(
      @Valid @RequestBody FileShareCreateAPIRequest request) {
    return fileShareResponseMapper.toResponse(
        fileService.createFileShare(request.fileId(), request.fromId(), request.toId()));
  }

  @DeleteMapping(value = "/{shareId}")
  @Operation(summary = "Delete file share", description = "Deletes a file share")
  @SecurityRequirement(name = BASIC_AUTH)
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "204", description = "Successfully deleted file share")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteFileShare(@PathVariable UUID shareId) {
    fileService.deleteFileShare(shareId);
  }
}
