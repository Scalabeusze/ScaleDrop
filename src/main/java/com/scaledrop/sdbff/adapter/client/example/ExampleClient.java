package com.scaledrop.sdbff.adapter.client.example;

import com.scaledrop.sdbff.adapter.client.example.configuration.ExampleClientConfiguration;
import com.scaledrop.sdbff.configuration.feign.ScaleDropFeignConfiguration;
import com.scaledrop.sdbff.domain.example.ExampleObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
    name = "example-service-client",
    url = "${example-service.url}",
    configuration = {ScaleDropFeignConfiguration.class, ExampleClientConfiguration.class}
)
public interface ExampleClient {

  String EXAMPLE_URL = "/internal/api/v1/example";

  @GetMapping(EXAMPLE_URL)
  ExampleObject getExampleObject();
}
