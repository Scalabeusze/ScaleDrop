package com.scaledrop.sdbff.adapter.client.account;

import com.scaledrop.sdbff.application.port.out.AccountRepository;
import com.scaledrop.sdbff.configuration.cache.RedisCacheConfig;
import com.scaledrop.sdbff.domain.account.AccountObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepository {

    private final AccountClient accountClient;

    @Override
    @Cacheable(
        value = RedisCacheConfig.ACCOUNT_OBJECT_CACHE,
        key = "#accountId"
    )
    public AccountObject getAccountObject(UUID accountId) {
        log.info("[ACCOUNT-ADAPTER] Calling external account service for ID: {}", accountId);
        return accountClient.getAccountObject(accountId.toString());
    }

}
