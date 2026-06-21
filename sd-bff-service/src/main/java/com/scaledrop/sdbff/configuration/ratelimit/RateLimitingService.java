package com.scaledrop.sdbff.configuration.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class RateLimitingService {

  private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

  public Bucket resolveBucket(String key, int capacity, int refillTokens, int refillMinutes) {
    return cache.computeIfAbsent(key, k -> newBucket(capacity, refillTokens, refillMinutes));
  }

  private Bucket newBucket(int capacity, int refillTokens, int refillMinutes) {
    Bandwidth limit =
        Bandwidth.builder()
            .capacity(capacity)
            .refillGreedy(refillTokens, Duration.ofMinutes(refillMinutes))
            .build();

    return Bucket.builder().addLimit(limit).build();
  }
}
