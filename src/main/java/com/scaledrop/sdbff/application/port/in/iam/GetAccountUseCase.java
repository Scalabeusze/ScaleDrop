package com.scaledrop.sdbff.application.port.in.iam;

import com.scaledrop.sdbff.adapter.api.model.iam.response.AccountIAMResponse;
import java.util.List;
import java.util.UUID;

public interface GetAccountUseCase {
  List<AccountIAMResponse> getAllAccounts();

  AccountIAMResponse getAccountById(UUID accountId);
}
