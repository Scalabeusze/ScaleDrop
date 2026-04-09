package com.scaledrop.sdbff.adapter.client.example;

import com.scaledrop.sdbff.application.port.out.ExampleRepository;
import com.scaledrop.sdbff.configuration.cache.RedisCacheConfig;
import com.scaledrop.sdbff.domain.example.ExampleObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExampleRepositoryAdapter implements ExampleRepository {

  private final ExampleClient exampleClient;

  @Override
  @Cacheable(
      value = RedisCacheConfig.EXAMPLE_OBJECT_CACHE,
      key = "'exampleObject'" // can be based on params
  )
  public ExampleObject getExampleObject() {
    return exampleClient.getExampleObject();
  }
}
