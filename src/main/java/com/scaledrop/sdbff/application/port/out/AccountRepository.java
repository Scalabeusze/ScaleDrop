package com.scaledrop.sdbff.application.port.out;

import java.util.UUID;
import com.scaledrop.sdbff.domain.account.AccountObject;

public interface AccountRepository {
  AccountObject getAccountObject(UUID accountId);
}
