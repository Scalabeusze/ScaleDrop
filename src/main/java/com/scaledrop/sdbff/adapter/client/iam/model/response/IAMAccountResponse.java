package com.scaledrop.sdbff.adapter.client.iam.model.response;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Builder
@ToString
@Validated
@NoArgsConstructor
@AllArgsConstructor
public class IAMAccountResponse {

  private UUID id;
  private String username;
  private String firstName;
  private String lastName;
  private String avatarUrl;
  private String status;
  private OffsetDateTime lastLoginAt;
}
