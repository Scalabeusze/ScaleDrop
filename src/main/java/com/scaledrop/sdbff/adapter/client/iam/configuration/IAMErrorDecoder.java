package com.scaledrop.sdbff.adapter.client.iam.configuration;

import com.scaledrop.sdbff.configuration.exception.iam.AccountNotFoundException;
import com.scaledrop.sdbff.configuration.exception.iam.IAMServiceException;
import com.scaledrop.sdbff.configuration.exception.iam.LoginException;
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
    log.error("[IAM-CLIENT] IAM Communication error: Method={}, Status={}", methodKey, status);

    if (status == HttpStatus.UNAUTHORIZED) {
      return new LoginException(
          "Token expired, invalid or missing. Authentication with IAM failed.");
    }

    if (status == HttpStatus.NOT_FOUND) {
      return new AccountNotFoundException("Account not found in IAM for the provided credentials.");
    }

    if (status.is4xxClientError() || status.is5xxServerError()) {
      return new IAMServiceException(
          "IAM service Exception: " + status.value() + " " + status.getReasonPhrase());
    }

    return defaultErrorDecoder.decode(methodKey, response);
  }
}
