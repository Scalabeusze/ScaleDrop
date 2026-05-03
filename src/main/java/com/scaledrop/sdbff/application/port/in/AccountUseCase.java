package com.scaledrop.sdbff.application.port.in;

import java.util.UUID;
import com.scaledrop.sdbff.domain.account.AccountObject;

public interface AccountUseCase {
  AccountObject getAccountObject(UUID accountId);
}
