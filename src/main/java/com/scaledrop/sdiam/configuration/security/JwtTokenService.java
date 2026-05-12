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

import static java.nio.charset.StandardCharsets.UTF_8;

import com.scaledrop.sdiam.configuration.TimeProvider;
import com.scaledrop.sdiam.configuration.exception.AccountServiceException;
import com.scaledrop.sdiam.configuration.security.properties.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

  public static final String EXPIRES_AT_CLAIM = "expires_at";
  public static final String ACCOUNT_ID_CLAIM = "account_id";

  private static final String JWT_SECRET_REQUIRED = "JWT secret is required";
  private static final String JWT_TTL_REQUIRED = "JWT ttl is required";
  private static final String JWT_SECRET_TOO_SHORT =
      "JWT secret must be at least 32 bytes for HS256";
  private static final int HS256_MIN_SECRET_BYTES = 32;

  private final JwtProperties jwtProperties;
  private final TimeProvider timeProvider;

  public String createToken(SessionAccountPrincipal principal) {
    Date now = new Date(timeProvider.now().toInstant().toEpochMilli());
    Date expiresAt = new Date(now.getTime() + getTtl().toMillis());

    return Jwts.builder()
        .subject(principal.getAccountId().toString())
        .issuedAt(now)
        .expiration(expiresAt)
        .claim("provider", principal.getProvider().name())
        .claim("role", "ROLE_USER")
        .signWith(getSigningKey(), Jwts.SIG.HS256)
        .compact();
  }

  private Duration getTtl() {
    Duration ttl = jwtProperties.getTtl();
    if (ttl == null) {
      throw new AccountServiceException(JWT_TTL_REQUIRED);
    }
    return ttl;
  }

  private SecretKey getSigningKey() {
    String secret = jwtProperties.getSecret();
    if (StringUtils.isBlank(secret)) {
      throw new AccountServiceException(JWT_SECRET_REQUIRED);
    }

    byte[] secretBytes = secret.getBytes(UTF_8);
    if (secretBytes.length < HS256_MIN_SECRET_BYTES) {
      throw new AccountServiceException(JWT_SECRET_TOO_SHORT);
    }

    return Keys.hmacShaKeyFor(secretBytes);
  }
}
