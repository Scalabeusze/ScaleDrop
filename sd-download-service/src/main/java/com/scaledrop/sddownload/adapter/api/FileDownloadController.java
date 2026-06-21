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

import com.scaledrop.sddownload.adapter.api.mapper.FileDownloadResponseMapper;
import com.scaledrop.sddownload.adapter.api.model.response.FileDownloadAPIResponse;
import com.scaledrop.sddownload.application.service.FileService;
import com.scaledrop.sddownload.configuration.annotations.DefaultApiExceptionResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(FileDownloadController.FILE_DOWNLOADS_ENDPOINT)
@Tag(name = "File downloads", description = "File downloads controller")
public class FileDownloadController {

  static final String FILE_DOWNLOADS_ENDPOINT = API_V1_PREFIX + "/file-downloads";

  private final FileService fileService;
  private final FileDownloadResponseMapper fileDownloadResponseMapper;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "List file downloads", description = "Lists file download history")
  @SecurityRequirement(name = BASIC_AUTH)
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "200", description = "Successfully fetched file downloads")
  @ResponseStatus(HttpStatus.OK)
  public List<FileDownloadAPIResponse> listFileDownloads(
      @RequestParam(value = "fileId", required = false) UUID fileId,
      @RequestParam(value = "ownerId", required = false) UUID ownerId,
      @RequestParam(value = "limit", required = true, defaultValue = "1000") @Min(0) @Max(1000) Integer limit,
      @RequestParam(value = "offset", required = true, defaultValue = "0") @Min(0) Integer offset) {
    return fileService.listFileDownloads(fileId, ownerId, limit, offset).stream()
        .map(fileDownloadResponseMapper::toResponse)
        .toList();
  }
}
