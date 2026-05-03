package com.scaledrop.sdbff.application.port.in;

import com.scaledrop.sdbff.domain.account.AccountObject;
import java.util.UUID;

public interface AccountUseCase {
  AccountObject getAccountObject(UUID accountId);
}
