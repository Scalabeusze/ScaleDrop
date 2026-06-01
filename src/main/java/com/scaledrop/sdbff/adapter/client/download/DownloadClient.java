package com.scaledrop.sdbff.adapter.client.download;

import com.scaledrop.sdbff.adapter.api.model.download.request.FileShareCreateRequest;
import com.scaledrop.sdbff.adapter.api.model.download.response.FileAPIResponse;
import com.scaledrop.sdbff.adapter.api.model.download.response.FileDownloadAPIResponse;
import com.scaledrop.sdbff.adapter.api.model.download.response.FileShareResponse;
import com.scaledrop.sdbff.adapter.client.download.configuration.DownloadClientConfiguration;
import feign.Response;
import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "download-service-client",
    url = "${download-service.url}",
    configuration = {DownloadClientConfiguration.class})
public interface DownloadClient {

  String FILES_URL = "/api/v1/files";
  String DOWNLOADS_URL = "/api/v1/file-downloads";
  String SHARES_URL = "/api/v1/file-shares";

  @GetMapping(FILES_URL)
  List<FileAPIResponse> listFiles(
      @RequestParam(value = "prefix", required = false) String prefix,
      @RequestParam(value = "ownerId", required = false) UUID ownerId,
      @RequestParam(value = "limit", required = false, defaultValue = "1000") Integer limit,
      @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset);

  @GetMapping(FILES_URL + "/{fileId}")
  FileAPIResponse getFile(@PathVariable("fileId") UUID fileId);

  @GetMapping(FILES_URL + "/{fileId}/download")
  Response getDownloadObjectResponse(@PathVariable("fileId") UUID fileId);

  @GetMapping(FILES_URL + "/sync")
  List<FileAPIResponse> syncFiles();

  @GetMapping(DOWNLOADS_URL)
  List<FileDownloadAPIResponse> listFileDownloads(
      @RequestParam(value = "fileId", required = false) UUID fileId,
      @RequestParam(value = "ownerId", required = false) UUID ownerId,
      @RequestParam(value = "limit", required = false, defaultValue = "1000") Integer limit,
      @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset);

  @GetMapping(SHARES_URL)
  List<FileShareResponse> listFileShares(
      @RequestParam(value = "fromId", required = false) UUID fromId,
      @RequestParam(value = "toId", required = false) UUID toId,
      @RequestParam(value = "limit", required = false, defaultValue = "1000") Integer limit,
      @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset);

  @PostMapping(SHARES_URL)
  FileShareResponse createFileShare(@RequestBody FileShareCreateRequest request);

  @DeleteMapping(SHARES_URL + "/{shareId}")
  void deleteFileShare(@PathVariable("shareId") UUID shareId);
}
