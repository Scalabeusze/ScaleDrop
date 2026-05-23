package com.scaledrop.sdbff.adapter.client.upload;

import com.scaledrop.sdbff.adapter.api.model.upload.request.RegisterUploadRequest;
import com.scaledrop.sdbff.adapter.api.model.upload.response.RegisterUploadResponse;
import com.scaledrop.sdbff.adapter.client.upload.configuration.UploadClientConfiguration;
import com.scaledrop.sdbff.configuration.feign.ScaleDropFeignConfiguration;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
    name = "upload-service-client",
    url = "${upload-service.url}",
    configuration = {ScaleDropFeignConfiguration.class, UploadClientConfiguration.class})
public interface UploadClient {

  String UPLOADS_BASE_URL = "/api/v1/upload";

  @PostMapping(value = UPLOADS_BASE_URL, consumes = "application/json")
  RegisterUploadResponse registerUpload(
      @RequestHeader("X-User-Id") UUID ownerId, @RequestBody RegisterUploadRequest request);

  @PostMapping(value = UPLOADS_BASE_URL + "/{fileId}/confirm")
  void confirmUpload(@PathVariable("fileId") UUID fileId);

  @DeleteMapping(value = UPLOADS_BASE_URL + "/{fileId}")
  void deleteUpload(@RequestHeader("X-User-Id") UUID ownerId, @PathVariable("fileId") UUID fileId);
}
