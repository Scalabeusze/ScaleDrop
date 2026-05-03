package com.scaledrop.sdbff.adapter.client.auth.configuration;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaledrop.sdbff.configuration.exception.SdBffServiceException;
import com.scaledrop.sdbff.configuration.exception.api.ApiExceptionResponse;
import feign.Response;
import feign.Response.Body;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Slf4j
public record AuthErrorDecoder(ObjectMapper objectMapper) implements ErrorDecoder {

  public Exception decode(String methodKey, Response response) {
    return Optional.ofNullable(response)
        .map(
            res -> {
              String requestUrl = res.request().url();
              HttpStatus status = HttpStatus.resolve(res.status());

              ApiExceptionResponse apiExceptionResponse = parseBody(res.body());
              log.info(
                  "Error on url: {}, status: {}, body: {}",
                  requestUrl,
                  status,
                  apiExceptionResponse);
              return new SdBffServiceException(apiExceptionResponse.getMessage());
            })
        .orElseThrow(() -> new SdBffServiceException("Empty error response from IAM"));
  }

  private ApiExceptionResponse parseBody(Body body) {
    try {
      return objectMapper.readValue(body.asReader(UTF_8), ApiExceptionResponse.class);
    } catch (IOException ex) {
      throw new SdBffServiceException("Error on parsing body", ex);
    }
  }
}
