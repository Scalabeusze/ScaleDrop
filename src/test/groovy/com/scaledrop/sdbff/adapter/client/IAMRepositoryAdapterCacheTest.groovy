package com.scaledrop.sdbff.adapter.client

import com.scaledrop.sdbff.IntegrationTestBase
import com.scaledrop.sdbff.adapter.client.iam.IAMClient
import com.scaledrop.sdbff.adapter.client.iam.IAMRepositoryAdapter
import com.scaledrop.sdbff.adapter.client.iam.model.request.IAMUpdateAccountRequest
import com.scaledrop.sdbff.adapter.client.iam.model.response.IAMAccountResponse
import com.scaledrop.sdbff.configuration.cache.RedisCacheConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.spockframework.spring.SpringBean

@SpringBootTest(classes = [CacheTestConfig])
class IAMRepositoryAdapterCacheTest extends IntegrationTestBase {

  @Autowired
  IAMRepositoryAdapter iamRepositoryAdapter

  @Autowired
  CacheManager cacheManager

  @SpringBean
  IAMClient iamClient = Mock()

  def setup() {
    cacheManager.getCache(RedisCacheConfig.ACCOUNT_DETAILS_CACHE)?.clear()
  }

  def "should cache getAccountById result and call client only once"() {
    given: "An account ID and a response"
      def accountId = UUID.randomUUID()
      def response = IAMAccountResponse.builder().id(accountId).username("test@example.com").build()

    when: "Calling the method twice"
      def result1 = iamRepositoryAdapter.getAccountById(accountId)
      def result2 = iamRepositoryAdapter.getAccountById(accountId)

    then: "IAMClient is called exactly ONCE"
      1 * iamClient.getAccount(accountId) >> response
      0 * iamClient._

    and: "Both results match"
      result1 == response
      result2 == response
  }

  def "should cache getAccountByUsername result"() {
    given:
      def username = "test@example.com"
      def response = IAMAccountResponse.builder().id(UUID.randomUUID()).username(username).build()

    when:
      def result1 = iamRepositoryAdapter.getAccountByUsername(username)
      def result2 = iamRepositoryAdapter.getAccountByUsername(username)

    then: "Client is called exactly ONCE"
      1 * iamClient.getAccountByUsername(username) >> response
      0 * iamClient._

    and:
      result1 == response
      result2 == response
  }

  def "should update cache for both ID and username on updateAccount"() {
    given:
      def accountId = UUID.randomUUID()
      def username = "updated@example.com"
      def request = IAMUpdateAccountRequest.builder().firstName("Marian").build()
      def response = IAMAccountResponse.builder().id(accountId).username(username).firstName("Marian").build()

    when: "Updating the account"
      iamRepositoryAdapter.updateAccount(accountId, request)

    and: "Fetching the same account by ID and Username"
      def fetchedById = iamRepositoryAdapter.getAccountById(accountId)
      def fetchedByUsername = iamRepositoryAdapter.getAccountByUsername(username)

    then: "Client's update endpoint is called"
      1 * iamClient.updateAccount(accountId, request) >> response

    and: "Client's GET endpoints are NEVER called because data was cached via @CachePut"
      0 * iamClient.getAccount(_)
      0 * iamClient.getAccountByUsername(_)

    and: "Fetched results are the updated response"
      fetchedById == response
      fetchedByUsername == response
  }

  def "should evict cache on deleteAccountById"() {
    given: "An account ID and a response"
      def accountId = UUID.randomUUID()
      def response = IAMAccountResponse.builder().id(accountId).username("test@example.com").build()

    when: "Prime the cache by fetching once"
      def firstFetch = iamRepositoryAdapter.getAccountById(accountId)

    then: "Client is called to populate the cache"
      1 * iamClient.getAccount(accountId) >> response
      firstFetch == response

    when: "Deleting the account"
      iamRepositoryAdapter.deleteAccountById(accountId)

    then: "Client's delete endpoint is called"
      1 * iamClient.deleteAccount(accountId)

    when: "Fetching the account again"
      def secondFetch = iamRepositoryAdapter.getAccountById(accountId)

    then: "Client's GET endpoint is called AGAIN because the cache was evicted"
      1 * iamClient.getAccount(accountId) >> response
      secondFetch == response
  }

  @TestConfiguration
  @EnableCaching
  static class CacheTestConfig {

    @Bean
    CacheManager cacheManager() {
      return new ConcurrentMapCacheManager(RedisCacheConfig.ACCOUNT_DETAILS_CACHE)
    }
  }
}
