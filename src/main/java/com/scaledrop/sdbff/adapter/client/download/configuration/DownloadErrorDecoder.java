package com.scaledrop.sdbff.adapter.client.download.configuration;

import com.scaledrop.sdbff.configuration.exception.download.DownloadNotFoundException;
import com.scaledrop.sdbff.configuration.exception.download.DownloadServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Slf4j
public class DownloadErrorDecoder implements ErrorDecoder {

  private final ErrorDecoder defaultErrorDecoder = new Default();

  @Override
  public Exception decode(String methodKey, Response response) {
    HttpStatus status = HttpStatus.valueOf(response.status());
    log.error(
        "[DOWNLOAD-CLIENT] Download communication error: Method={}, Status={}", methodKey, status);

    if (status == HttpStatus.NOT_FOUND) {
      return new DownloadNotFoundException(
          "Requested file or download history not found in external service.");
    }

    if (status == HttpStatus.BAD_REQUEST) {
      return new DownloadServiceException("Bad request sent to download service.");
    }

    if (status.is4xxClientError() || status.is5xxServerError()) {
      return new DownloadServiceException(
          "Download service Exception: " + status.value() + " " + status.getReasonPhrase());
    }

    return defaultErrorDecoder.decode(methodKey, response);
  }
}
