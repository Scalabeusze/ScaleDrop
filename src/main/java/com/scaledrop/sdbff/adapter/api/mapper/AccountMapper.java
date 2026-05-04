package com.scaledrop.sdbff.adapter.api.mapper;

import com.scaledrop.sdbff.adapter.api.model.iam.response.AccountIAMResponse;
import com.scaledrop.sdbff.domain.account.AccountObject;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

  public AccountObject toDomain(AccountIAMResponse response) {
    if (response == null) {
      return null;
    }
    return AccountObject.builder()
        .id(response.getId())
        .username(response.getUsername())
        .status(response.getStatus())
        .failedLoginAttempts(response.getFailedLoginAttempts())
        .lockedUntil(response.getLockedUntil())
        .lastLoginAt(response.getLastLoginAt())
        .passwordUpdatedAt(response.getPasswordUpdatedAt())
        .createdAt(response.getCreatedAt())
        .updatedAt(response.getUpdatedAt())
        .build();
  }

  public AccountIAMResponse toResponse(AccountObject domain) {
    if (domain == null) {
      return null;
    }
    return AccountIAMResponse.builder()
        .id(domain.getId())
        .username(domain.getUsername())
        .status(domain.getStatus())
        .failedLoginAttempts(domain.getFailedLoginAttempts())
        .lockedUntil(domain.getLockedUntil())
        .lastLoginAt(domain.getLastLoginAt())
        .passwordUpdatedAt(domain.getPasswordUpdatedAt())
        .createdAt(domain.getCreatedAt())
        .updatedAt(domain.getUpdatedAt())
        .build();
  }
}
