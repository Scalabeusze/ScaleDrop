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

package com.scaledrop.sdiam.configuration.security

import static com.scaledrop.sdiam.configuration.security.JwtTokenService.ACCOUNT_ID_CLAIM
import static com.scaledrop.sdiam.configuration.security.JwtTokenService.EXPIRES_AT_CLAIM
import static java.nio.charset.StandardCharsets.UTF_8

import com.scaledrop.sdiam.IntegrationTestBase
import com.scaledrop.sdiam.adapter.db.AccountEntity.AccountStatus
import com.scaledrop.sdiam.adapter.db.IdentityProvider
import com.scaledrop.sdiam.configuration.security.properties.JwtProperties
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.UUID
import org.springframework.beans.factory.annotation.Autowired

class JwtTokenServiceTest extends IntegrationTestBase {

  @Autowired
  private JwtTokenService jwtTokenService

  @Autowired
  private JwtProperties jwtProperties

  def "should create signed jwt with exact user id and expiration claims"() {
    given:
    def accountId = UUID.randomUUID()
    def principal =
        new SessionAccountPrincipal(accountId, "test_username", AccountStatus.ACTIVE, IdentityProvider.LOCAL)

    when:
    def jwt = jwtTokenService.createToken(principal)
    def claims = parseJwtClaims(jwt)

    then:
    claims.keySet() == [
      EXPIRES_AT_CLAIM,
      ACCOUNT_ID_CLAIM
    ] as Set
    claims[ACCOUNT_ID_CLAIM] == accountId.toString()
    claims[EXPIRES_AT_CLAIM] == "2022-10-11T15:00:00Z"
  }

  private Map parseJwtClaims(String jwt) {
    return Jwts.parser()
        .verifyWith(Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(UTF_8)))
        .build()
        .parseSignedClaims(jwt)
        .getPayload()
  }
}
