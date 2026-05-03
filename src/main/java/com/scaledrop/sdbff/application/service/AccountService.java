package com.scaledrop.sdbff.application.service;

import com.scaledrop.sdbff.application.port.in.AccountUseCase;
import com.scaledrop.sdbff.application.port.out.AccountRepository;
import com.scaledrop.sdbff.domain.account.AccountObject;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService implements AccountUseCase {

  private final AccountRepository accountRepository;

  @Override
  public AccountObject getAccountObject(UUID accountId) {
    log.warn("[ACCOUNT-SERVICE] Routing request to fetch account details for ID: {}", accountId);
    return accountRepository.getAccountObject(accountId);
  }

}
