package com.scaledrop.sdbff.application.port.out;

import com.scaledrop.sdbff.adapter.client.iam.model.request.IAMLoginRequest;
import com.scaledrop.sdbff.adapter.client.iam.model.request.IAMUpdateAccountRequest;
import com.scaledrop.sdbff.adapter.client.iam.model.response.IAMAccountResponse;
import com.scaledrop.sdbff.adapter.client.iam.model.response.IAMJWTResponse;
import com.scaledrop.sdbff.domain.account.AccountSearchResult;
import java.util.List;
import java.util.UUID;

public interface IAMRepository {

  IAMJWTResponse login(IAMLoginRequest request);

  IAMAccountResponse getAccountById(UUID accountId);

  IAMAccountResponse getAccountByUsername(String username);

  IAMAccountResponse updateAccount(UUID accountId, IAMUpdateAccountRequest request);

  void deleteAccountById(UUID accountId);

  List<AccountSearchResult> searchAccounts(String query, Integer limit);
}
