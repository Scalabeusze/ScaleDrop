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

package com.scaledrop.sdiam.application.service;

import com.scaledrop.sdiam.configuration.exception.AccountServiceException;
import com.scaledrop.sdiam.configuration.exception.AccountValidationException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Service;

@Service
public class AccountPasswordService {

  private static final String PASSWORD_HASHING_ERROR = "Unable to hash password";
  private static final String HASHING_ALGORITHM = "PBKDF2WithHmacSHA256";
  private static final int SALT_LENGTH_BYTES = 16;
  private static final int ITERATIONS = 65_536;
  private static final int KEY_LENGTH_BITS = 256;

  private final SecureRandom secureRandom = new SecureRandom();

  /**
   * Hashes a plain password
   *
   * @param plainPassword plain password to hash
   * @return Password's hash and salt
   * @throws AccountValidationException if new password does not meet validation requirements
   * @throws AccountServiceException if the service encounters an unexpected problem
   */
  public PasswordData hashPassword(String plainPassword) {
    byte[] salt = new byte[SALT_LENGTH_BYTES];
    secureRandom.nextBytes(salt);

    byte[] hash;
    try {
      PBEKeySpec keySpec =
          new PBEKeySpec(plainPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS);
      hash = SecretKeyFactory.getInstance(HASHING_ALGORITHM).generateSecret(keySpec).getEncoded();
    } catch (NoSuchAlgorithmException | InvalidKeySpecException exception) {
      throw new AccountServiceException(PASSWORD_HASHING_ERROR);
    }

    var base64Encoder = Base64.getEncoder();
    return new PasswordData(base64Encoder.encodeToString(hash), base64Encoder.encodeToString(salt));
  }

  public record PasswordData(String hash, String salt) {}
}
