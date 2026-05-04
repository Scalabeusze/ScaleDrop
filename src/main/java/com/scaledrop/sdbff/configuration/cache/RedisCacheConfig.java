package com.scaledrop.sdbff.configuration.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.scaledrop.sdbff.adapter.api.model.iam.response.AccountIAMResponse;
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
  public static final String ACCOUNT_DETAILS_CACHE = "accountDetails";

  private final StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
  private final Jackson2JsonRedisSerializer<Integer> integerJackson2JsonRedisSerializer =
      new Jackson2JsonRedisSerializer<>(Integer.class);

  @Primary
  @Bean(name = REDIS_CACHE_MANAGER)
  public CacheManager redisCacheManager(
      @Value("${app.config.cache.default-ttl-hours:1}") Long defaultCacheHours,
      @Value("${app.config.cache.audience-ttl-hours:12}") Long audienceCacheHours,
      @Value("${app.config.cache.account-object-ttl-hours:12}") Long accountDetailsCacheHours,
      RedisConnectionFactory redisConnectionFactory) {

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    Jackson2JsonRedisSerializer<AccountIAMResponse> accountResponseRedisSerializer =
        new Jackson2JsonRedisSerializer<>(objectMapper, AccountIAMResponse.class);

    final RedisCacheConfiguration config =
        RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(
                SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<>(Object.class)))
            .entryTtl(Duration.ofHours(defaultCacheHours));
    return RedisCacheManager.builder(redisConnectionFactory)
        .cacheDefaults(config)
        .withCacheConfiguration(
            ACCOUNT_DETAILS_CACHE,
            RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(SerializationPair.fromSerializer(stringRedisSerializer))
                .serializeValuesWith(
                    SerializationPair.fromSerializer(accountResponseRedisSerializer))
                .entryTtl(Duration.ofHours(accountDetailsCacheHours)))
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
