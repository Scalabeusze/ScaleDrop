package com.scaledrop.sdbff.application.port.in;

import com.scaledrop.sdbff.adapter.api.model.account.request.UpdateAccountAPIRequest;
import com.scaledrop.sdbff.adapter.client.iam.model.response.IAMAccountResponse;
import java.util.UUID;

public interface IAMUseCase {

  String login(String googleToken);

  IAMAccountResponse getAccountById(UUID accountId);

  IAMAccountResponse updateAccount(UUID accountId, UpdateAccountAPIRequest request);

  void deactivateAccount(UUID accountId);
}
