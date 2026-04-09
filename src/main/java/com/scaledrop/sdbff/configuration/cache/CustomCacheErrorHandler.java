package com.scaledrop.sdbff.configuration.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.lang.NonNull;

@Slf4j
public class CustomCacheErrorHandler implements CacheErrorHandler {

  @Override
  public void handleCacheGetError(@NonNull RuntimeException exception, @NonNull Cache cache, @NonNull Object key) {
    log.atWarn()
        .setMessage("Cache GET error for key: {} in cache: {}")
        .addArgument(key)
        .addArgument(cache::getName)
        .setCause(exception)
        .log();
  }

  @Override
  public void handleCachePutError(@NonNull RuntimeException exception, @NonNull Cache cache, @NonNull Object key,
      Object value) {
    log.atWarn()
        .setMessage("Cache PUT error for key: {} in cache: {}")
        .addArgument(key)
        .addArgument(cache::getName)
        .setCause(exception)
        .log();
  }

  @Override
  public void handleCacheEvictError(@NonNull RuntimeException exception, @NonNull Cache cache, @NonNull Object key) {
    log.atWarn()
        .setMessage("Cache EVICT error for key: {} in cache: {}")
        .addArgument(key)
        .addArgument(cache::getName)
        .setCause(exception)
        .log();
  }

  @Override
  public void handleCacheClearError(@NonNull RuntimeException exception, @NonNull Cache cache) {
    log.atWarn()
        .setMessage("Cache CLEAR error in cache: {}")
        .addArgument(cache::getName)
        .setCause(exception)
        .log();
  }
}
