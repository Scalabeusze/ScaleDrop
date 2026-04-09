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

package com.scaledrop.sdiam.configuration.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.lang.annotation.*;

@NotBlank(message = "Password cannot be empty") @Size(min = 8, message = "Password must be at least 8 characters long") @Pattern.List({
  @Pattern(regexp = ".*[a-z].*", message = "Password must contain at least one lowercase letter"),
  @Pattern(regexp = ".*[A-Z].*", message = "Password must contain at least one uppercase letter"),
  @Pattern(regexp = ".*\\d.*", message = "Password must contain at least one number"),
  @Pattern(
      regexp = ".*[!@#$%^&+=].*",
      message = "Password must contain at least one special character (@#$%^&+=)")
})
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface ValidPassword {
  String message() default "Invalid password format";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
