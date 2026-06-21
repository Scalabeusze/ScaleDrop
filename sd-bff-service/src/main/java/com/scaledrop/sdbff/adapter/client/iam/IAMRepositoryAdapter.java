package com.scaledrop.sdbff.adapter.client.iam;

import com.scaledrop.sdbff.adapter.api.mapper.AccountMapper;
import com.scaledrop.sdbff.adapter.api.model.account.response.AccountSearchAPIResponse;
import com.scaledrop.sdbff.adapter.client.iam.model.request.IAMLoginRequest;
import com.scaledrop.sdbff.adapter.client.iam.model.request.IAMUpdateAccountRequest;
import com.scaledrop.sdbff.adapter.client.iam.model.response.IAMAccountResponse;
import com.scaledrop.sdbff.adapter.client.iam.model.response.IAMJWTResponse;
import com.scaledrop.sdbff.application.port.out.IAMRepository;
import com.scaledrop.sdbff.configuration.cache.RedisCacheConfig;
import com.scaledrop.sdbff.domain.account.AccountSearchResult;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IAMRepositoryAdapter implements IAMRepository {

  private final IAMClient iamClient;
  private final AccountMapper accountMapper;

  @Override
  public IAMJWTResponse login(IAMLoginRequest request) {
    log.debug("[IAM-CLIENT] sending login request: {}", request);
    return iamClient.login(request);
  }

  @Override
  @Cacheable(cacheNames = RedisCacheConfig.ACCOUNT_DETAILS_CACHE, key = "#accountId")
  public IAMAccountResponse getAccountById(UUID accountId) {
    log.debug("[IAM-CLIENT] getting account by ID: {}", accountId);
    return iamClient.getAccount(accountId);
  }

  @Override
  @Cacheable(cacheNames = RedisCacheConfig.ACCOUNT_DETAILS_CACHE, key = "#username")
  public IAMAccountResponse getAccountByUsername(String username) {
    log.debug("[IAM-CLIENT] getting account by username: {}", username);
    return iamClient.getAccountByUsername(username);
  }

  @Override
  @Caching(
      put = {
        @CachePut(cacheNames = RedisCacheConfig.ACCOUNT_DETAILS_CACHE, key = "#accountId"),
        @CachePut(
            cacheNames = RedisCacheConfig.ACCOUNT_DETAILS_CACHE,
            key = "#result.username") // Update username entry
        // also
      })
  public IAMAccountResponse updateAccount(UUID accountId, IAMUpdateAccountRequest request) {
    log.debug("[IAM-CLIENT] sending update user {} request: {}", accountId, request);
    return iamClient.updateAccount(accountId, request);
  }

  @Override
  @CacheEvict(cacheNames = RedisCacheConfig.ACCOUNT_DETAILS_CACHE, key = "#accountId")
  public void deleteAccountById(UUID accountId) {
    log.debug("[IAM-CLIENT] deleting account by ID: {}", accountId);
    iamClient.deleteAccount(accountId);
  }

  @Override
  public List<AccountSearchResult> searchAccounts(String query, Integer limit) {
    List<AccountSearchAPIResponse> responses = iamClient.searchAccounts(query, limit);
    return responses.stream().map(accountMapper::toDomain).toList();
  }
}
