package com.scaledrop.sdbff.adapter.client.iam.configuration;

import com.scaledrop.sdbff.configuration.exception.SdBffServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Slf4j
public class IAMErrorDecoder implements ErrorDecoder {

  private final ErrorDecoder defaultErrorDecoder = new Default();

  @Override
  public Exception decode(String methodKey, Response response) {
    HttpStatus status = HttpStatus.valueOf(response.status());
    log.error("[IAM-CLIENT] Błąd komunikacji z IAM: Metoda={}, Status={}", methodKey, status);
    if (status.is4xxClientError() || status.is5xxServerError()) {
      return new SdBffServiceException(
          "Błąd z serwisu IAM: " + status.value() + " " + status.getReasonPhrase());
    }

    return defaultErrorDecoder.decode(methodKey, response);
  }
}
