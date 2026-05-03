package com.scaledrop.sdbff.adapter.client.auth;

import com.scaledrop.sdbff.application.port.out.AuthRepository;
import com.scaledrop.sdbff.domain.auth.TokenObject;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthRepositoryAdapter implements AuthRepository {

  private final AuthClient authClient;

  @Override
  public TokenObject getTokenObject(String gcode) {
    return authClient.exchangeCode(Map.of("gcode", gcode));
  }
}
