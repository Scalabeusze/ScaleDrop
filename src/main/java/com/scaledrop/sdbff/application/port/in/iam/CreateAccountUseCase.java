package com.scaledrop.sdbff.application.port.in.iam;

import com.scaledrop.sdbff.adapter.api.model.iam.request.CreateAccountIAMRequest;
import com.scaledrop.sdbff.adapter.api.model.iam.response.AccountIAMResponse;

public interface CreateAccountUseCase {
  AccountIAMResponse createAccount(CreateAccountIAMRequest request);
}
