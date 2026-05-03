package com.scaledrop.sdbff.adapter.api.mapper;

import com.scaledrop.sdbff.adapter.api.model.response.LoginAPIResponse;
import com.scaledrop.sdbff.configuration.MapperConfiguration;
import com.scaledrop.sdbff.domain.auth.TokenObject;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface AuthResponseMapper {

    LoginAPIResponse toResponse(TokenObject tokenObject);
}