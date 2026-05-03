package com.scaledrop.sdbff.adapter.client.auth;

import com.scaledrop.sdbff.adapter.client.auth.configuration.AuthClientConfiguration;
import com.scaledrop.sdbff.configuration.feign.ScaleDropFeignConfiguration;
import com.scaledrop.sdbff.domain.auth.TokenObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(
    name = "auth-service-client",
    url = "${iam-service.url}",
    configuration = {ScaleDropFeignConfiguration.class, AuthClientConfiguration.class}
)
public interface AuthClient {

  String AUTH_URL = "/internal/api/v1/auth";

  @PostMapping(value = AUTH_URL + "/login")
  TokenObject exchangeCode(@RequestBody Map<String, String> body);
}