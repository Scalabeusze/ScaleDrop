package com.scaledrop.sdbff.adapter.api.mapper;

import com.scaledrop.sdbff.adapter.api.model.response.AccountAPIResponse;
import com.scaledrop.sdbff.configuration.MapperConfiguration;
import com.scaledrop.sdbff.domain.account.AccountObject;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface AccountResponseMapper {

  AccountAPIResponse toResponse(AccountObject accountObject);
}