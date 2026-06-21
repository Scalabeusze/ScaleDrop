package com.scaledrop.sdbff.adapter.client.upload.configuration;

import com.scaledrop.sdbff.configuration.exception.upload.UploadNotFoundException;
import com.scaledrop.sdbff.configuration.exception.upload.UploadServiceException;
import com.scaledrop.sdbff.configuration.exception.upload.UploadValidationException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Slf4j
public class UploadErrorDecoder implements ErrorDecoder {

  private final ErrorDecoder defaultErrorDecoder = new Default();

  @Override
  public Exception decode(String methodKey, Response response) {
    HttpStatus status = HttpStatus.valueOf(response.status());
    log.error(
        "[UPLOAD-CLIENT] Upload Communication error: Method={}, Status={}", methodKey, status);

    if (status == HttpStatus.NOT_FOUND) {
      return new UploadNotFoundException("Upload not found for the provided ID.");
    }

    if (status == HttpStatus.BAD_REQUEST || status == HttpStatus.UNPROCESSABLE_ENTITY) {
      return new UploadValidationException("Upload request validation failed.");
    }

    if (status.is4xxClientError() || status.is5xxServerError()) {
      return new UploadServiceException(
          "Upload service Exception: " + status.value() + " " + status.getReasonPhrase());
    }

    return defaultErrorDecoder.decode(methodKey, response);
  }
}
