package com.scaledrop.sdbff.application.port.in.iam;

import com.scaledrop.sdbff.adapter.api.model.iam.request.UpdatePasswordIAMRequest;
import com.scaledrop.sdbff.adapter.api.model.iam.response.AccountIAMResponse;
import java.util.UUID;

public interface UpdatePasswordUseCase {
  AccountIAMResponse updatePassword(UUID accountId, UpdatePasswordIAMRequest request);
}
