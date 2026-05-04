package com.scaledrop.sdbff.application.port.out;

import com.scaledrop.sdbff.adapter.api.model.iam.request.CreateAccountIAMRequest;
import com.scaledrop.sdbff.adapter.api.model.iam.request.SessionLoginIAMRequest;
import com.scaledrop.sdbff.adapter.api.model.iam.request.UpdateAccountIAMRequest;
import com.scaledrop.sdbff.adapter.api.model.iam.request.UpdatePasswordIAMRequest;
import com.scaledrop.sdbff.adapter.api.model.iam.response.AccountIAMResponse;
import com.scaledrop.sdbff.adapter.api.model.iam.response.JwtIAMResponse;
import java.util.List;
import java.util.UUID;

public interface IAMRepository {

  JwtIAMResponse login(SessionLoginIAMRequest request);

  List<AccountIAMResponse> getAllAccounts();

  AccountIAMResponse getAccountById(UUID accountId);

  AccountIAMResponse createAccount(CreateAccountIAMRequest request);

  AccountIAMResponse updateAccount(UUID accountId, UpdateAccountIAMRequest request);

  AccountIAMResponse updatePassword(UUID accountId, UpdatePasswordIAMRequest request);

  void deleteAccount(UUID accountId);
}
