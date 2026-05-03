package com.scaledrop.sdbff.application.service;

import com.scaledrop.sdbff.application.port.in.AuthUseCase;
import com.scaledrop.sdbff.application.port.out.AuthRepository;
import com.scaledrop.sdbff.domain.auth.TokenObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

  private final AuthRepository authRepository;

  @Override
  public TokenObject login(String gcode) {
    log.warn("[AUTH-SERVICE] Routing request to fetch account details for ID: {}", gcode);
    return authRepository.getTokenObject(gcode);
  }
}