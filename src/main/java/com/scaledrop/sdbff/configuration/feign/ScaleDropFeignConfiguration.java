package com.scaledrop.sdbff.configuration.feign;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Retryer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.MDC;
import org.springframework.boot.info.BuildProperties;
import org.springframework.cloud.openfeign.encoding.BaseRequestInterceptor;
import org.springframework.cloud.openfeign.encoding.FeignClientEncodingProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ScaleDropFeignConfiguration {

  private static final String REQUEST_BODY = "requestBody";
  private static final String REQUEST_CLIENT = "requestClient";
  private static final String REQUEST_HEADERS = "requestHeaders";

  private final BuildProperties buildProperties;

  @Bean
  public RequestInterceptor userAgentHeaderInterceptor() {
    return template -> template.header(HttpHeaders.USER_AGENT, getUserAgent());
  }

  private String getUserAgent() {
    return String.format("%s/%s", buildProperties.getName(), buildProperties.getVersion());
  }

  @Bean
  public RequestInterceptor requestLogger(FeignClientEncodingProperties properties) {
    return new BaseRequestInterceptor(properties) {
      @Override
      public void apply(RequestTemplate template) {
        Request request = template.feignTarget().apply(template);
        try {
          byte[] body = request.body();
          MDC.put(REQUEST_BODY, ArrayUtils.isNotEmpty(body) ? new String(body) : "");
          MDC.put(REQUEST_CLIENT, template.feignTarget().name());
          MDC.put(REQUEST_HEADERS, collectHeaders(request));

          log.info("Sending {} request to url: {}", request.httpMethod(), request.url());
        } finally {
          MDC.remove(REQUEST_BODY);
          MDC.remove(REQUEST_CLIENT);
          MDC.remove(REQUEST_HEADERS);
        }
      }

      private String collectHeaders(Request request) {
        return request.headers().entrySet().stream()
            .filter(header -> !header.getKey().equalsIgnoreCase(AUTHORIZATION))
            .toList().toString();
      }
    };
  }

  @Bean
  public FeignClientEncodingProperties properties() {
    return new FeignClientEncodingProperties();
  }

  @Bean
  public Retryer retryer() {
    return new Retryer.Default(200, SECONDS.toMillis(1), 3);
  }
}
