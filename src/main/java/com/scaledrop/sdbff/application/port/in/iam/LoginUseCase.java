package com.scaledrop.sdbff.application.port.in.iam;

import com.scaledrop.sdbff.adapter.api.model.iam.request.SessionLoginIAMRequest;
import com.scaledrop.sdbff.adapter.api.model.iam.response.JwtIAMResponse;

public interface LoginUseCase {
  JwtIAMResponse login(SessionLoginIAMRequest request);
}
