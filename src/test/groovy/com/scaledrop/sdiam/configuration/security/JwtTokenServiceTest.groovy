package com.scaledrop.sdiam.configuration.security
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

import com.scaledrop.sdiam.adapter.db.AccountEntity.AccountStatus
import com.scaledrop.sdiam.adapter.db.IdentityProvider
import com.scaledrop.sdiam.configuration.TimeProvider
import com.scaledrop.sdiam.configuration.security.properties.JwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Clock
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneOffset
import spock.lang.Specification

class JwtTokenServiceTest extends Specification {

  def jwtProperties = Mock(JwtProperties)
  def timeProvider = Mock(TimeProvider)

  def jwtTokenService = new JwtTokenService(jwtProperties, timeProvider)

  def "should create signed jwt with standard claims and exact expiration"() {
    given: "Test data and mocked environment"
    def accountId = UUID.randomUUID()
    def principal = new SessionAccountPrincipal(
        accountId,
        "test_username",
        AccountStatus.ACTIVE,
        IdentityProvider.LOCAL
        )

    def fixedNow = OffsetDateTime.of(2022, 10, 11, 14, 0, 0, 0, ZoneOffset.UTC)
    def ttl = Duration.ofHours(1)
    def secret = "very-long-safe-super-duper-secret-key-for-jwt-signing-32-bytes-min"

    jwtProperties.getSecret() >> secret
    jwtProperties.getTtl() >> ttl
    timeProvider.now() >> fixedNow

    when: "Generating and parsing token"
    def jwt = jwtTokenService.createToken(principal)
    def claims = parseJwtClaims(jwt, secret, fixedNow)

    then: "Token contains correct standard and custom claims"
    claims.getSubject() == accountId.toString()
    claims.getExpiration() == Date.from(fixedNow.plus(ttl).toInstant())
    claims.getIssuedAt() == Date.from(fixedNow.toInstant())

    claims.get("provider", String.class) == IdentityProvider.LOCAL.name()
    claims.get("role", String.class) == "ROLE_USER"
  }

  private static Claims parseJwtClaims(String jwt, String secret, OffsetDateTime fixedNow) {
    return Jwts.parser()
        .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
        .clock({ -> Date.from(fixedNow.toInstant()) } as Clock)
        .build()
        .parseSignedClaims(jwt)
        .getPayload()
  }
}
