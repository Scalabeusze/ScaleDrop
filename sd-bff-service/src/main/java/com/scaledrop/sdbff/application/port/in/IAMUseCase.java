package com.scaledrop.sdbff.application.port.in;

import com.scaledrop.sdbff.adapter.api.model.account.request.UpdateAccountAPIRequest;
import com.scaledrop.sdbff.adapter.client.iam.model.response.IAMAccountResponse;
import com.scaledrop.sdbff.domain.account.AccountSearchResult;
import java.util.List;
import java.util.UUID;

public interface IAMUseCase {

  String login(String googleToken);

  IAMAccountResponse getAccountById(UUID accountId);

  IAMAccountResponse updateAccount(UUID accountId, UpdateAccountAPIRequest request);

  void deactivateAccount(UUID accountId);

  List<AccountSearchResult> searchAccounts(String query, Integer limit);
}
