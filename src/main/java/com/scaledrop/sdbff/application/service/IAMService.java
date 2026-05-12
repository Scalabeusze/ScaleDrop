package com.scaledrop.sdbff.application.service;

import com.scaledrop.sdbff.adapter.client.iam.model.request.IAMLoginRequest;
import com.scaledrop.sdbff.application.port.in.IAMUseCase;
import com.scaledrop.sdbff.application.port.out.IAMRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IAMService implements IAMUseCase {

  private final IAMRepository iamRepository;

  @Override
  public String login(String googleToken) {
    return iamRepository.login(IAMLoginRequest.from(googleToken)).getJwt();
  }
}
