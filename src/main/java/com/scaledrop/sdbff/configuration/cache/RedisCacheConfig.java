package com.scaledrop.sdbff.configuration.cache;

import com.scaledrop.sdbff.domain.account.AccountObject;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration(proxyBeanMethods = false)
public class RedisCacheConfig {

  public static final String AUDIENCE_CACHE = "audienceCache";
  public static final String REDIS_CACHE_MANAGER = "redisCacheManager";
  public static final String ACCOUNT_OBJECT_CACHE = "accountObjectCache";

  private final StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
  private final Jackson2JsonRedisSerializer<Integer> integerJackson2JsonRedisSerializer =
      new Jackson2JsonRedisSerializer<>(Integer.class);
  private final Jackson2JsonRedisSerializer<AccountObject> accountObjectRedisSerializer =
      new Jackson2JsonRedisSerializer<>(AccountObject.class);

  @Primary
  @Bean(name = REDIS_CACHE_MANAGER)
  public CacheManager redisCacheManager(
      @Value("${app.config.cache.default-ttl-hours:1}") Long defaultCacheHours,
      @Value("${app.config.cache.audience-ttl-hours:12}") Long audienceCacheHours,
      @Value("${app.config.cache.example-object-ttl-hours:12}") Long exampleObjectCacheHours,
      @Value("${app.config.cache.account-object-ttl-hours:12}") Long accountObjectCacheHours,
      @Value("${app.config.cache.upload-object-ttl-hours:12}") Long uploadObjectCacheHours,
      RedisConnectionFactory redisConnectionFactory) {
    final RedisCacheConfiguration config =
        RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(
                SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<>(Object.class)))
            .entryTtl(Duration.ofHours(defaultCacheHours));
    return RedisCacheManager.builder(redisConnectionFactory)
        .cacheDefaults(config)
        .withCacheConfiguration(
            ACCOUNT_OBJECT_CACHE,
            RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(SerializationPair.fromSerializer(stringRedisSerializer))
                .serializeValuesWith(SerializationPair.fromSerializer(accountObjectRedisSerializer))
                .entryTtl(Duration.ofHours(accountObjectCacheHours)))
        .withCacheConfiguration(
            AUDIENCE_CACHE,
            RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(SerializationPair.fromSerializer(stringRedisSerializer))
                .serializeValuesWith(
                    SerializationPair.fromSerializer(integerJackson2JsonRedisSerializer))
                .entryTtl(Duration.ofHours(audienceCacheHours)))
        .cacheWriter(RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory))
        .build();
  }

  @Bean
  public RedisTemplate<String, Integer> redisTemplate(
      RedisConnectionFactory redisConnectionFactory) {
    final RedisTemplate<String, Integer> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory);
    redisTemplate.setKeySerializer(stringRedisSerializer);
    redisTemplate.setValueSerializer(integerJackson2JsonRedisSerializer);
    return redisTemplate;
  }
}
