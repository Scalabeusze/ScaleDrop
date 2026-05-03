package com.scaledrop.sdbff.adapter.client.upload;

import com.scaledrop.sdbff.adapter.client.upload.configuration.UploadClientConfiguration;
import com.scaledrop.sdbff.configuration.feign.ScaleDropFeignConfiguration;
import com.scaledrop.sdbff.domain.upload.UploadObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "upload-service-client",
    url = "${upload-service.url}",
    configuration = {ScaleDropFeignConfiguration.class, UploadClientConfiguration.class})
public interface UploadClient {

  String UPLOAD_URL = "/internal/api/v1/upload";

  @PostMapping(value = UPLOAD_URL + "/get-upload-url")
  String getUploadUrl(@RequestBody UploadObject metadata);
}
