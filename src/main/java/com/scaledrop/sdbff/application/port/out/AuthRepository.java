package com.scaledrop.sdbff.application.port.out;

import com.scaledrop.sdbff.domain.auth.TokenObject;

public interface AuthRepository {

    TokenObject getTokenObject(String gcode);
}