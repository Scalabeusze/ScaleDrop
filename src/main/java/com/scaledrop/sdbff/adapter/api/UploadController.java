package com.scaledrop.sdbff.adapter.api;

import static com.scaledrop.sdbff.configuration.Constants.API_V1_PREFIX;

import com.scaledrop.sdbff.adapter.api.mapper.UploadRequestMapper;
import com.scaledrop.sdbff.adapter.api.model.upload.request.RegisterUploadRequest;
import com.scaledrop.sdbff.adapter.api.model.upload.response.RegisterUploadResponse;
import com.scaledrop.sdbff.application.port.in.upload.UploadUseCase;
import com.scaledrop.sdbff.configuration.annotations.DefaultApiExceptionResponses;
import com.scaledrop.sdbff.configuration.annotations.DefaultApiSecurity;
import com.scaledrop.sdbff.domain.upload.UploadObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "Upload", description = "File upload management (S3 & SNS integration)")
public class UploadController {

  private static final String UPLOAD_ENDPOINT = API_V1_PREFIX + "/upload";

  private final UploadUseCase uploadUseCase;
  private final UploadRequestMapper uploadRequestMapper;

  @PostMapping(UPLOAD_ENDPOINT)
  @Operation(
      summary = "Register upload",
      description =
          "Registers metadata for file or folder and returns a pre-signed S3 URL if applicable. "
              + "Owner ID is automatically extracted from the JWT token.")
  @DefaultApiSecurity
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "201", description = "Successfully registered upload")
  @ResponseStatus(HttpStatus.CREATED)
  public RegisterUploadResponse registerUpload(
      @Valid @RequestBody RegisterUploadRequest request, @AuthenticationPrincipal Jwt jwt) {

    UUID ownerId = UUID.fromString(jwt.getSubject());

    UploadObject uploadObject = uploadRequestMapper.toDomain(request);

    uploadObject.setOwnerId(ownerId);

    log.info(
        "[BFF-CONTROLLER] Received {} registration request: {} for owner: {}",
        uploadObject.getType(),
        uploadObject.getName(),
        ownerId);

    return uploadUseCase.registerUpload(uploadObject);
  }

  @PostMapping(UPLOAD_ENDPOINT + "/{fileId}/confirm")
  @Operation(
      summary = "Confirm upload",
      description =
          "Triggers the finalization of the upload process. "
              + "Updates status and triggers SNS notification in the background.")
  @DefaultApiSecurity
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "200", description = "Upload confirmed")
  @ResponseStatus(HttpStatus.OK)
  public void confirmUpload(@PathVariable("fileId") UUID fileId) {
    log.info("[BFF-CONTROLLER] Confirming upload for file ID: {}", fileId);

    uploadUseCase.confirmUpload(fileId);
  }
}
