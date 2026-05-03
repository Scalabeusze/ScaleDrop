package com.scaledrop.sdbff.application.port.out;

import com.scaledrop.sdbff.domain.account.AccountObject;
import java.util.UUID;

public interface AccountRepository {
  AccountObject getAccountObject(UUID accountId);
}
