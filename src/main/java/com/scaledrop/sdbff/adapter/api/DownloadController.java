package com.scaledrop.sdbff.adapter.api;

import static com.scaledrop.sdbff.configuration.Constants.API_V1_PREFIX;
import static com.scaledrop.sdbff.configuration.Constants.BASIC_AUTH;

import com.scaledrop.sdbff.adapter.api.mapper.DownloadResponseMapper;
import com.scaledrop.sdbff.adapter.api.model.response.DownloadAPIResponse;
import com.scaledrop.sdbff.application.port.in.DownloadUseCase;
import com.scaledrop.sdbff.configuration.annotations.DefaultApiExceptionResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "Download", description = "Download controller")
public class DownloadController {

  private static final String DOWNLOAD_ENDPOINT = API_V1_PREFIX + "/download/{fileId}";

  private final DownloadUseCase downloadUseCase;
  private final DownloadResponseMapper downloadResponseMapper;

  @GetMapping(value = DOWNLOAD_ENDPOINT)
  @Operation(
      summary = "Download file",
      description = "Streams file from download service by its ID")
  @SecurityRequirement(name = BASIC_AUTH)
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "200", description = "Successfully fetched file")
  @ResponseStatus(HttpStatus.OK)
  public DownloadAPIResponse getDownloadUrl(@PathVariable("fileId") UUID fileId) {
    log.info("[DOWNLOAD-CONTROLLER] Requesting download link for file ID: {}", fileId);
    return downloadResponseMapper.toResponse(downloadUseCase.getDownloadObject(fileId));
  }
}
