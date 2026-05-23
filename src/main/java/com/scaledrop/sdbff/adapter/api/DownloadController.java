package com.scaledrop.sdbff.adapter.api;

import static com.scaledrop.sdbff.configuration.Constants.API_V1_PREFIX;

import com.scaledrop.sdbff.application.port.in.DownloadUseCase;
import com.scaledrop.sdbff.configuration.annotations.DefaultApiExceptionResponses;
import com.scaledrop.sdbff.configuration.annotations.DefaultApiSecurity;
import com.scaledrop.sdbff.configuration.ratelimit.UserRateLimit;
import com.scaledrop.sdbff.domain.download.FileDownloadHistory;
import com.scaledrop.sdbff.domain.download.FileObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "Download", description = "File downloading and history management")
public class DownloadController {

  private static final String FILES_ENDPOINT = API_V1_PREFIX + "/files";
  private static final String FILE_DOWNLOADS_ENDPOINT = API_V1_PREFIX + "/file-downloads";

  private final DownloadUseCase downloadUseCase;

  @UserRateLimit
  @GetMapping(FILES_ENDPOINT)
  @Operation(
      summary = "List files",
      description = "Lists files for the logged-in user with optional prefix filtering.")
  @DefaultApiSecurity
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "200", description = "Successfully fetched files")
  @ResponseStatus(HttpStatus.OK)
  public List<FileObject> listFiles(
      @RequestParam(value = "prefix", required = false) String prefix,
      @RequestParam(value = "limit", required = false, defaultValue = "1000") Integer limit,
      @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
      @AuthenticationPrincipal Jwt jwt) {

    UUID ownerId = UUID.fromString(jwt.getSubject());

    log.info(
        "[BFF-CONTROLLER] Received file list request for owner: {} with prefix: {}",
        ownerId,
        prefix);

    return downloadUseCase.listFiles(prefix, ownerId, limit, offset);
  }

  @UserRateLimit
  @GetMapping(FILES_ENDPOINT + "/{fileId}")
  @Operation(summary = "Get file details", description = "Fetches a specific file's metadata.")
  @DefaultApiSecurity
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "200", description = "Successfully fetched file metadata")
  @ResponseStatus(HttpStatus.OK)
  public FileObject getFile(@PathVariable UUID fileId) {

    log.info("[BFF-CONTROLLER] Received file details request for ID: {}", fileId);

    return downloadUseCase.getFile(fileId);
  }

  @UserRateLimit(capacity = 10, refillTokens = 10, refillMinutes = 1)
  @GetMapping(FILES_ENDPOINT + "/{fileId}/download")
  @Operation(
      summary = "Download file",
      description = "Redirects to the pre-signed S3 download URL for the requested file.")
  @DefaultApiSecurity
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "302", description = "Redirect to pre-signed S3 download URL")
  @ResponseStatus(HttpStatus.FOUND)
  public ResponseEntity<Void> downloadFile(@PathVariable UUID fileId) {

    log.info("[BFF-CONTROLLER] Received download request for file ID: {}", fileId);

    String downloadUrl = downloadUseCase.downloadFile(fileId);

    return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(downloadUrl)).build();
  }

  @UserRateLimit(capacity = 2, refillTokens = 2, refillMinutes = 1)
  @GetMapping(FILES_ENDPOINT + "/sync")
  @Operation(
      summary = "Sync files",
      description = "Triggers a manual S3 to Database synchronization process.")
  @DefaultApiSecurity
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "200", description = "Successfully synced files")
  @ResponseStatus(HttpStatus.OK)
  public List<FileObject> syncFiles() {

    log.info("[BFF-CONTROLLER] Received manual file synchronization request");

    return downloadUseCase.syncFiles();
  }

  @UserRateLimit
  @GetMapping(FILE_DOWNLOADS_ENDPOINT)
  @Operation(
      summary = "List file downloads",
      description = "Lists file download history for the user.")
  @DefaultApiSecurity
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "200", description = "Successfully fetched download history")
  @ResponseStatus(HttpStatus.OK)
  public List<FileDownloadHistory> listFileDownloads(
      @RequestParam(value = "fileId", required = false) UUID fileId,
      @RequestParam(value = "limit", required = false, defaultValue = "1000") Integer limit,
      @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
      @AuthenticationPrincipal Jwt jwt) {

    UUID ownerId = UUID.fromString(jwt.getSubject());

    log.info("[BFF-CONTROLLER] Received download history request for owner: {}", ownerId);

    return downloadUseCase.listFileDownloads(fileId, ownerId, limit, offset);
  }
}
