package com.scaledrop.sdbff.application.port.in.iam;

import java.util.UUID;

public interface DeleteAccountUseCase {
  void deleteAccount(UUID accountId);
}
