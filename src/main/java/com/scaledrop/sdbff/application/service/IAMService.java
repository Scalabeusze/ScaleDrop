package com.scaledrop.sdbff.application.service;

import com.scaledrop.sdbff.adapter.api.model.account.request.UpdateAccountAPIRequest;
import com.scaledrop.sdbff.adapter.client.iam.model.request.IAMLoginRequest;
import com.scaledrop.sdbff.adapter.client.iam.model.request.IAMUpdateAccountRequest;
import com.scaledrop.sdbff.adapter.client.iam.model.response.IAMAccountResponse;
import com.scaledrop.sdbff.application.port.in.IAMUseCase;
import com.scaledrop.sdbff.application.port.out.IAMRepository;
import com.scaledrop.sdbff.domain.account.AccountSearchResult;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IAMService implements IAMUseCase {

  private final IAMRepository iamRepository;

  @Override
  public String login(String googleToken) {
    return iamRepository.login(IAMLoginRequest.from(googleToken)).getJwt();
  }

  @Override
  public IAMAccountResponse getAccountById(UUID accountId) {
    return iamRepository.getAccountById(accountId);
  }

  @Override
  public IAMAccountResponse updateAccount(UUID accountId, UpdateAccountAPIRequest request) {
    return iamRepository.updateAccount(accountId, IAMUpdateAccountRequest.from(request));
  }

  @Override
  public void deactivateAccount(UUID accountId) {
    iamRepository.deleteAccountById(accountId);
  }

  @Override
  public List<AccountSearchResult> searchAccounts(String query, Integer limit) {
    return iamRepository.searchAccounts(query, limit);
  }
}
