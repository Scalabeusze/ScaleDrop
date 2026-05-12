package com.scaledrop.sdbff.adapter.client.iam;

import com.scaledrop.sdbff.adapter.api.model.iam.request.*;
import com.scaledrop.sdbff.adapter.api.model.iam.response.*;
import com.scaledrop.sdbff.adapter.client.iam.model.request.IAMLoginRequest;
import com.scaledrop.sdbff.adapter.client.iam.model.request.IAMUpdateAccountRequest;
import com.scaledrop.sdbff.adapter.client.iam.model.response.IAMAccountResponse;
import com.scaledrop.sdbff.adapter.client.iam.model.response.IAMJWTResponse;
import com.scaledrop.sdbff.application.port.out.IAMRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IAMRepositoryAdapter implements IAMRepository {

  private final IAMClient iamClient;

  @Override
  public IAMJWTResponse login(IAMLoginRequest request) {
    log.debug("[IAM-CLIENT] sending login request: {}", request);
    return iamClient.login(request);
  }

  @Override
  public IAMAccountResponse getAccountById(UUID accountId) {
    log.debug("[IAM-CLIENT] getting account by ID: {}", accountId);
    return iamClient.getAccount(accountId);
  }

  @Override
  public IAMAccountResponse getAccountByUsername(String username) {
    log.debug("[IAM-CLIENT] getting account by username: {}", username);
    return iamClient.getAccountByUsername(username);
  }

  @Override
  public IAMAccountResponse updateAccount(UUID accountId, IAMUpdateAccountRequest request) {
    log.debug("[IAM-CLIENT] sending update user {} request: {}", accountId, request);
    return iamClient.updateAccount(accountId, request);
  }

  @Override
  public void deleteAccountById(UUID accountId) {
    log.debug("[IAM-CLIENT] deleting account by ID: {}", accountId);
    iamClient.deleteAccount(accountId);
  }

  @Override
  public JwtIAMResponse login(SessionLoginIAMRequest request) {
    log.debug("[IAM-ADAPTER] Wywołanie Feign Clienta dla logowania");
    return iamClient.login(request);
  }

  @Override
  public List<AccountIAMResponse> getAllAccounts() {
    return iamClient.getAccounts();
  }

  @Override
  @Cacheable(cacheNames = "accountDetails", key = "#accountId")
  public AccountIAMResponse getAccountByIdOld(UUID accountId) {
    return iamClient.getAccountOld(accountId);
  }

  @Override
  public AccountIAMResponse createAccount(CreateAccountIAMRequest request) {
    return iamClient.createAccount(request);
  }

  @Override
  @CacheEvict(cacheNames = "accountDetails", key = "#accountId")
  public AccountIAMResponse updateAccount(UUID accountId, UpdateAccountIAMRequest request) {
    return iamClient.updateAccount(accountId, request);
  }

  @Override
  @CacheEvict(cacheNames = "accountDetails", key = "#accountId")
  public AccountIAMResponse updatePassword(UUID accountId, UpdatePasswordIAMRequest request) {
    return iamClient.updatePassword(accountId, request);
  }

  @Override
  @CacheEvict(cacheNames = "accountDetails", key = "#accountId")
  public void deleteAccount(UUID accountId) {
    iamClient.deleteAccountOld(accountId);
  }
}
