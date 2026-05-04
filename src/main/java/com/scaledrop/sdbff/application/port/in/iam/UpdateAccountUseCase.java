package com.scaledrop.sdbff.application.port.in.iam;

import com.scaledrop.sdbff.adapter.api.model.iam.request.UpdateAccountIAMRequest;
import com.scaledrop.sdbff.adapter.api.model.iam.response.AccountIAMResponse;
import java.util.UUID;

public interface UpdateAccountUseCase {
  AccountIAMResponse updateAccount(UUID accountId, UpdateAccountIAMRequest request);
}
