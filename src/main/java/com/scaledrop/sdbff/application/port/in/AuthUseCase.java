package com.scaledrop.sdbff.application.port.in;

import com.scaledrop.sdbff.domain.auth.TokenObject;

public interface AuthUseCase {
    TokenObject login(String gcode);
}