/*
 * Copyright 2026-present Scalabeusze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.scaledrop.sdiam.configuration.security;

import com.scaledrop.sdiam.adapter.db.AccountEntity;
import com.scaledrop.sdiam.adapter.db.AccountEntity.AccountStatus;
import com.scaledrop.sdiam.adapter.db.IdentityProvider;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Getter
@RequiredArgsConstructor
public class SessionAccountPrincipal implements Serializable {

  private final UUID accountId;
  private final String username;
  private final AccountStatus status;
  private final IdentityProvider provider;

  public static SessionAccountPrincipal from(AccountEntity account, IdentityProvider provider) {
    return new SessionAccountPrincipal(
        account.getId(), account.getUsername(), account.getStatus(), provider);
  }

  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_USER"));
  }
}
