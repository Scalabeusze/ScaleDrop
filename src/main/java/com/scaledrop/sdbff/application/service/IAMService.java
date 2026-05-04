package com.scaledrop.sdbff.application.service;

import com.scaledrop.sdbff.adapter.api.model.iam.request.*;
import com.scaledrop.sdbff.adapter.api.model.iam.response.*;
import com.scaledrop.sdbff.application.port.in.iam.*;
import com.scaledrop.sdbff.application.port.out.IAMRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IAMService
    implements LoginUseCase,
        CreateAccountUseCase,
        GetAccountUseCase,
        UpdateAccountUseCase,
        UpdatePasswordUseCase,
        DeleteAccountUseCase {

  private final IAMRepository iamRepository;

  @Override
  public JwtIAMResponse login(SessionLoginIAMRequest request) {
    log.info("[IAM-SERVICE] Processing login request for: {}", request.getUsername());
    return iamRepository.login(request);
  }

  @Override
  public List<AccountIAMResponse> getAllAccounts() {
    log.info("[IAM-SERVICE] Fetching list of all accounts");
    return iamRepository.getAllAccounts();
  }

  @Override
  public AccountIAMResponse getAccountById(UUID accountId) {
    log.info("[IAM-SERVICE] Fetching data for account ID: {}", accountId);
    return iamRepository.getAccountById(accountId);
  }

  @Override
  public AccountIAMResponse createAccount(CreateAccountIAMRequest request) {
    log.info("[IAM-SERVICE] Creating a new account for: {}", request.getUsername());
    return iamRepository.createAccount(request);
  }

  @Override
  public AccountIAMResponse updateAccount(UUID accountId, UpdateAccountIAMRequest request) {
    log.info("[IAM-SERVICE] Updating data for account ID: {}", accountId);
    return iamRepository.updateAccount(accountId, request);
  }

  @Override
  public AccountIAMResponse updatePassword(UUID accountId, UpdatePasswordIAMRequest request) {
    log.info("[IAM-SERVICE] Updating password for account ID: {}", accountId);
    return iamRepository.updatePassword(accountId, request);
  }

  @Override
  public void deleteAccount(UUID accountId) {
    log.info("[IAM-SERVICE] Deleting account ID: {}", accountId);
    iamRepository.deleteAccount(accountId);
  }
}
