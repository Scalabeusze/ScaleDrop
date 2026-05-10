package com.scaledrop.sdbff.application.port.in.iam;

import com.scaledrop.sdbff.adapter.api.model.iam.response.AccountIAMResponse;
import java.util.List;

public interface GetAllAccountsUseCase {

  List<AccountIAMResponse> getAllAccounts();
}
