package com.scaledrop.sdbff.application.port.out;

import com.scaledrop.sdbff.adapter.api.model.iam.request.CreateAccountIAMRequest;
import com.scaledrop.sdbff.adapter.api.model.iam.request.SessionLoginIAMRequest;
import com.scaledrop.sdbff.adapter.api.model.iam.request.UpdateAccountIAMRequest;
import com.scaledrop.sdbff.adapter.api.model.iam.request.UpdatePasswordIAMRequest;
import com.scaledrop.sdbff.adapter.api.model.iam.response.AccountIAMResponse;
import com.scaledrop.sdbff.adapter.api.model.iam.response.JwtIAMResponse;
import com.scaledrop.sdbff.adapter.client.iam.model.request.IAMLoginRequest;
import com.scaledrop.sdbff.adapter.client.iam.model.request.IAMUpdateAccountRequest;
import com.scaledrop.sdbff.adapter.client.iam.model.response.IAMAccountResponse;
import com.scaledrop.sdbff.adapter.client.iam.model.response.IAMJWTResponse;
import java.util.List;
import java.util.UUID;

public interface IAMRepository {

  IAMJWTResponse login(IAMLoginRequest request);

  IAMAccountResponse getAccountById(UUID accountId);

  IAMAccountResponse getAccountByUsername(String username);

  IAMAccountResponse updateAccount(UUID accountId, IAMUpdateAccountRequest request);

  void deleteAccountById(UUID accountId);

  JwtIAMResponse login(SessionLoginIAMRequest request);

  List<AccountIAMResponse> getAllAccounts();

  AccountIAMResponse getAccountByIdOld(UUID accountId);

  AccountIAMResponse createAccount(CreateAccountIAMRequest request);

  AccountIAMResponse updateAccount(UUID accountId, UpdateAccountIAMRequest request);

  AccountIAMResponse updatePassword(UUID accountId, UpdatePasswordIAMRequest request);

  void deleteAccount(UUID accountId);
}
