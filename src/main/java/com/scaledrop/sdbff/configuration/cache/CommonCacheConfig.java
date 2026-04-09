package com.scaledrop.sdbff.configuration.cache;

import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonCacheConfig implements CachingConfigurer {

  @Override
  public CustomCacheErrorHandler errorHandler() {
    return new CustomCacheErrorHandler();
  }
}
