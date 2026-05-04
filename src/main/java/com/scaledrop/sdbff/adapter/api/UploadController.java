package com.scaledrop.sdbff.adapter.api;

import static com.scaledrop.sdbff.configuration.Constants.API_V1_PREFIX;
import static com.scaledrop.sdbff.configuration.Constants.BEARER_AUTH;

import com.scaledrop.sdbff.adapter.api.mapper.UploadRequestMapper;
import com.scaledrop.sdbff.adapter.api.model.upload.request.UploadAPIRequest;
import com.scaledrop.sdbff.application.port.in.upload.UploadUseCase;
import com.scaledrop.sdbff.configuration.annotations.DefaultApiExceptionResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "Upload", description = "Upload controller")
public class UploadController {

  private static final String UPLOAD_ENDPOINT = API_V1_PREFIX + "/upload";

  private final UploadUseCase uploadUseCase;
  private final UploadRequestMapper uploadRequestMapper;

  @PostMapping(value = UPLOAD_ENDPOINT, consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Initialize upload",
      description =
          "Generates a pre-signed URL. Owner ID is automatically extracted from JWT token.")
  @SecurityRequirement(name = BEARER_AUTH)
  @DefaultApiExceptionResponses
  @ApiResponse(responseCode = "200", description = "Successfully generated pre-signed URL")
  @ResponseStatus(HttpStatus.OK)
  public String getUploadUrl(
      @Valid @RequestBody UploadAPIRequest request, @AuthenticationPrincipal Jwt jwt) {
    UUID ownerId = UUID.fromString(jwt.getSubject());
    log.info(
        "[UPLOAD-CONTROLLER] Received upload initialization for file: {} by owner: {}",
        request.getFileName(),
        ownerId);
    var uploadObject = uploadRequestMapper.toDomain(request);
    uploadObject.setOwnerId(ownerId);
    return uploadUseCase.getUploadUrl(uploadObject);
  }
}
