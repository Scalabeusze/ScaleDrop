package com.scaledrop.sdbff.adapter.api.mapper;

import com.scaledrop.sdbff.adapter.api.model.account.response.AccountAPIResponse;
import com.scaledrop.sdbff.adapter.api.model.account.response.JwtAPIResponse;
import com.scaledrop.sdbff.adapter.client.iam.model.response.IAMAccountResponse;
import com.scaledrop.sdbff.configuration.MapperConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface AccountMapper {

  JwtAPIResponse toJwtResponse(String jwt);

  AccountAPIResponse toAccountResponse(IAMAccountResponse iamAccountResponse);
}
