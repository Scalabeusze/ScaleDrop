package com.scaledrop.sdbff.adapter.client.account;

import com.scaledrop.sdbff.adapter.client.account.configuration.AccountClientConfiguration;
import com.scaledrop.sdbff.configuration.feign.ScaleDropFeignConfiguration;
import com.scaledrop.sdbff.domain.account.AccountObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "iam-service-client",
    url = "${iam-service.url}",
    configuration = {ScaleDropFeignConfiguration.class, AccountClientConfiguration.class}
)
public interface AccountClient {
    
  String ACCOUNT_URL = "/internal/api/v1/account";

  @GetMapping(ACCOUNT_URL + "/{id}")
  AccountObject getAccountObject(@PathVariable("id") String id);
}
