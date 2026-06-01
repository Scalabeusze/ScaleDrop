package com.scaledrop.sdbff.adapter.api;

import static com.scaledrop.sdbff.configuration.Constants.API_V1_PREFIX;

import com.scaledrop.sdbff.adapter.api.model.download.request.FileShareCreateRequest;
import com.scaledrop.sdbff.application.port.in.DownloadUseCase;
import com.scaledrop.sdbff.configuration.annotations.DefaultApiExceptionResponses;
import com.scaledrop.sdbff.configuration.annotations.DefaultApiSecurity;
import com.scaledrop.sdbff.configuration.ratelimit.UserRateLimit;
import com.scaledrop.sdbff.domain.download.FileDownloadHistory;
import com.scaledrop.sdbff.domain.download.FileObject;
import com.scaledrop.sdbff.domain.download.FileShare;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
  private static final String FILE_SHARES_ENDPOINT = API_V1_PREFIX + "/file-shares";

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

  @UserRateLimit
  @GetMapping(FILE_SHARES_ENDPOINT)
  @Operation(
      summary = "List file shares",
      description = "Lists file shares indicating who shared files with whom.")
  @DefaultApiSecurity
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "200", description = "Successfully fetched file shares")
  @ResponseStatus(HttpStatus.OK)
  public List<FileShare> listFileShares(
      @RequestParam(value = "fromId", required = false) UUID fromId,
      @RequestParam(value = "toId", required = false) UUID toId,
      @RequestParam(value = "limit", required = false, defaultValue = "1000") Integer limit,
      @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset) {

    log.info(
        "[BFF-CONTROLLER] Received list file shares request. fromId={}, toId={}", fromId, toId);

    return downloadUseCase.listFileShares(fromId, toId, limit, offset);
  }

  @UserRateLimit
  @PostMapping(FILE_SHARES_ENDPOINT)
  @Operation(
      summary = "Create file share",
      description =
          "Creates a new file share. Sharer ID is automatically extracted from the JWT token.")
  @DefaultApiSecurity
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "201", description = "Successfully created file share")
  @ResponseStatus(HttpStatus.CREATED)
  public FileShare createFileShare(
      @Valid @RequestBody FileShareCreateRequest request, @AuthenticationPrincipal Jwt jwt) {

    UUID fromId = UUID.fromString(jwt.getSubject());

    log.info(
        "[BFF-CONTROLLER] Received create file share request. fileId={}, fromId(JWT)={}, toId={}",
        request.fileId(),
        fromId,
        request.toId());

    return downloadUseCase.createFileShare(request.fileId(), fromId, request.toId());
  }

  @UserRateLimit
  @DeleteMapping(FILE_SHARES_ENDPOINT + "/{shareId}")
  @Operation(
      summary = "Delete file share",
      description = "Deletes a specific file share, revoking access to the file.")
  @DefaultApiSecurity
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "204", description = "Successfully deleted file share")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteFileShare(@PathVariable UUID shareId) {

    log.info("[BFF-CONTROLLER] Received delete file share request for shareId: {}", shareId);

    downloadUseCase.deleteFileShare(shareId);
  }
}
