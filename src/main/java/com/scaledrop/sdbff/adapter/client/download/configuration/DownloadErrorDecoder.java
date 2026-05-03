package com.scaledrop.sdbff.adapter.client.download.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaledrop.sdbff.configuration.exception.api.ApiExceptionResponse;
import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DownloadErrorDecoder implements ErrorDecoder {

  private final ObjectMapper objectMapper;
  private final ErrorDecoder defaultErrorDecoder = new Default();

  @Override
  public Exception decode(String methodKey, Response response) {
    try (InputStream bodyIs = response.body().asInputStream()) {
      ApiExceptionResponse errorResponse =
          objectMapper.readValue(bodyIs, ApiExceptionResponse.class);
      log.error(
          "[DOWNLOAD-CLIENT] Error occurred during file download. Message: {}, Type: {}",
          errorResponse.getMessage(),
          errorResponse.getType());

      return new RuntimeException(errorResponse.getMessage());
    } catch (IOException | RuntimeException e) {
      log.warn(
          "[DOWNLOAD-CLIENT] Could not parse error response, falling back to default decoder.");
      return defaultErrorDecoder.decode(methodKey, response);
    }
  }
}
