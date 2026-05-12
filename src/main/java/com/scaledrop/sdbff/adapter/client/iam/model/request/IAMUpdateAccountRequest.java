package com.scaledrop.sdbff.adapter.client.iam.model.request;

import com.scaledrop.sdbff.adapter.api.model.account.request.UpdateAccountAPIRequest;
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
public class IAMUpdateAccountRequest {

  private String firstName;
  private String lastName;
  private String avatarUrl;

  public static IAMUpdateAccountRequest from(UpdateAccountAPIRequest request) {
    return IAMUpdateAccountRequest.builder()
        .firstName(request.getFirstName())
        .lastName(request.getLastName())
        .avatarUrl(request.getAvatarUrl())
        .build();
  }
}
