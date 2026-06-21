package com.scaledrop.sdbff.configuration

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.scaledrop.sdbff.IntegrationTestBase
import com.scaledrop.sdbff.configuration.cache.CustomCacheErrorHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Service

import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.*

@Import(CustomCacheErrorHandlerTest.TestConfig)
class CustomCacheErrorHandlerTest extends IntegrationTestBase {

  @Autowired
  TestCachedService testCachedService

  @Autowired
  @Qualifier("testCacheManager")
  CacheManager testCacheManager

  ListAppender<ILoggingEvent> logAppender
  Logger logger

  def setup() {
    logger = (Logger) LoggerFactory.getLogger(CustomCacheErrorHandler)
    logAppender = new ListAppender<>()
    logAppender.start()
    logger.addAppender(logAppender)
  }

  def cleanup() {
    if (logger && logAppender) {
      logger.detachAppender(logAppender)
    }
  }

  def "should handle cache GET error and continue with normal flow"() {
    given:
      def cacheKey = "testKey"

    when:
      def result = testCachedService.getCachedValue(cacheKey)

    then:
      result == "value from service: testKey"
  }

  def "should handle cache PUT error and continue with normal flow"() {
    given:
      def cacheKey = "putKey"

    when:
      def result = testCachedService.getCachedValue(cacheKey)

    then:
      result == "value from service: putKey"
  }

  def "should handle cache EVICT error and continue with normal flow"() {
    given:
      def cacheKey = "evictKey"

    when:
      testCachedService.evictCache(cacheKey)

    then:
      noExceptionThrown()
  }

  def "should handle cache CLEAR error and continue with normal flow"() {
    when:
      testCachedService.clearCache()

    then:
      noExceptionThrown()
  }

  def "should log warning when cache GET fails"() {
    given:
      def cacheKey = "errorKey"
      Cache mockCache = mock(Cache)
      when(testCacheManager.getCache("testCache")).thenReturn(mockCache)
      when(mockCache.get(anyString())).thenThrow(new RuntimeException("Simulated cache GET error"))

    when:
      def result = testCachedService.getCachedValue(cacheKey)

    then:
      result == "value from service: errorKey"

    and:
      logAppender.list.any {
        it.level == Level.WARN &&
            it.message.contains("Cache GET error")
      }
  }

  def "should log warning when cache PUT fails"() {
    given:
      def cacheKey = "putErrorKey"
      Cache mockCache = mock(Cache)
      when(testCacheManager.getCache("testCache")).thenReturn(mockCache)
      doThrow(new RuntimeException("Simulated cache PUT error"))
          .when(mockCache).put(anyString(), any())

    when:
      def result = testCachedService.getCachedValue(cacheKey)

    then:
      result == "value from service: putErrorKey"

    and:
      logAppender.list.any {
        it.level == Level.WARN &&
            it.message.contains("Cache PUT error")
      }
  }

  def "should log warning when cache EVICT fails"() {
    given:
      def cacheKey = "evictErrorKey"
      Cache mockCache = mock(Cache)
      when(testCacheManager.getCache("testCache")).thenReturn(mockCache)
      doThrow(new RuntimeException("Simulated cache EVICT error"))
          .when(mockCache).evict(anyString())

    when:
      testCachedService.evictCache(cacheKey)

    then:
      noExceptionThrown()

    and:
      logAppender.list.any {
        it.level == Level.WARN &&
            it.message.contains("Cache EVICT error")
      }
  }

  def "should log warning when cache CLEAR fails"() {
    given:
      Cache mockCache = mock(Cache)
      when(testCacheManager.getCache("testCache")).thenReturn(mockCache)
      doThrow(new RuntimeException("Simulated cache CLEAR error"))
          .when(mockCache).clear()

    when:
      testCachedService.clearCache()

    then:
      noExceptionThrown()

    and:
      logAppender.list.any {
        it.level == Level.WARN &&
            it.message.contains("Cache CLEAR error")
      }
  }

  @TestConfiguration
  static class TestConfig {

    @Bean
    TestCachedService testCachedService() {
      new TestCachedService()
    }

    @Bean(name = "testCacheManager")
    CacheManager testCacheManager() {
      spy(new ConcurrentMapCacheManager("testCache"))
    }
  }

  @Service
  static class TestCachedService {

    @Cacheable(value = "testCache", cacheManager = "testCacheManager")
    String getCachedValue(String key) {
      "value from service: $key"
    }

    @CacheEvict(value = "testCache", cacheManager = "testCacheManager")
    void evictCache(String key) {
    }

    @CacheEvict(value = "testCache", cacheManager = "testCacheManager", allEntries = true)
    void clearCache() {
    }
  }
}
