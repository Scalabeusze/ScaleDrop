/*
 * Copyright 2026-present Scalabeusze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.scaledrop.sddownload.adapter.api

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import com.scaledrop.sddownload.WiremockTestBase
import com.scaledrop.sddownload.adapter.db.FileEntity
import com.scaledrop.sddownload.adapter.db.FileRepository
import com.scaledrop.sddownload.utilities.Initializer
import groovy.json.JsonSlurper
import java.time.OffsetDateTime
import org.springframework.beans.factory.annotation.Autowired
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client

class FileControllerTest extends WiremockTestBase {

  private static final String FILES_ENDPOINT = "/api/v1/files"

  @Autowired
  S3Client s3Client

  @Autowired
  FileRepository fileRepository

  def setup() {
    fileRepository.deleteAll()
  }

  def "should list all files from database when prefix is not provided"() {
    given:
    def report = persistFile("exports/list/report.csv")
    def archive = persistFile("exports/list/archive.csv")
    def other = persistFile("exports/other/report.csv")

    when:
    def result = mockMvc.perform(get(FILES_ENDPOINT)
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isOk())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response*.key as Set == [
      report.key,
      archive.key,
      other.key
    ] as Set
    response*.fileId as Set == [
      report.id.toString(),
      archive.id.toString(),
      other.id.toString()
    ] as Set
    response.every { it.size != null }
    response.every { it.lastModified != null }
    response.every { it.eTag != null }
    response.every { it.ownerId != null }
    response.every { it.name != null }
    response.every { it.location != null }
    response.every { it.contentType != null }
    response.every { it.status == "UPLOADED" }
  }

  def "should list files from database by optional prefix"() {
    given:
    persistFile("exports/prefix/report.csv")
    persistFile("exports/prefix/archive.csv")
    persistFile("exports/other/report.csv")

    when:
    def result = mockMvc.perform(get(FILES_ENDPOINT)
        .param("prefix", "exports/prefix/")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isOk())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response*.key as Set == [
      "exports/prefix/report.csv",
      "exports/prefix/archive.csv"
    ] as Set
  }

  def "should list files from database by optional owner id"() {
    given:
    def ownerId = UUID.randomUUID()
    persistFile("exports/owner/report.csv", ownerId)
    persistFile("exports/owner/archive.csv", ownerId)
    persistFile("exports/owner/other-user.csv", UUID.randomUUID())

    when:
    def result = mockMvc.perform(get(FILES_ENDPOINT)
        .param("ownerId", ownerId.toString())
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isOk())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response*.key as Set == [
      "exports/owner/report.csv",
      "exports/owner/archive.csv"
    ] as Set
    response.every { it.ownerId == ownerId.toString() }
  }

  def "should list files from database by optional owner id and prefix"() {
    given:
    def ownerId = UUID.randomUUID()
    persistFile("exports/combined/report.csv", ownerId)
    persistFile("exports/combined/archive.csv", ownerId)
    persistFile("exports/other/report.csv", ownerId)
    persistFile("exports/combined/other-user.csv", UUID.randomUUID())

    when:
    def result = mockMvc.perform(get(FILES_ENDPOINT)
        .param("ownerId", ownerId.toString())
        .param("prefix", "exports/combined/")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isOk())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response*.key as Set == [
      "exports/combined/report.csv",
      "exports/combined/archive.csv"
    ] as Set
    response.every { it.ownerId == ownerId.toString() }
  }

  def "should list files from database by optional limit"() {
    given:
    persistFile("exports/paging/01.csv")
    persistFile("exports/paging/02.csv")
    persistFile("exports/paging/03.csv")

    when:
    def result = mockMvc.perform(get(FILES_ENDPOINT)
        .param("limit", "2")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isOk())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response*.key == [
      "exports/paging/01.csv",
      "exports/paging/02.csv"
    ]
  }

  def "should list files from database by optional limit and offset"() {
    given:
    persistFile("exports/paging/01.csv")
    persistFile("exports/paging/02.csv")
    persistFile("exports/paging/03.csv")
    persistFile("exports/paging/04.csv")

    when:
    def result = mockMvc.perform(get(FILES_ENDPOINT)
        .param("limit", "2")
        .param("offset", "1")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isOk())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response*.key == [
      "exports/paging/02.csv",
      "exports/paging/03.csv"
    ]
  }

  def "should return empty list when limit is zero"() {
    given:
    persistFile("exports/paging/01.csv")

    when:
    def result = mockMvc.perform(get(FILES_ENDPOINT)
        .param("limit", "0")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isOk())
        .andReturn()

    then:
    parseJson(result.response.contentAsString).isEmpty()
  }

  def "should list files from database by owner id and prefix with paging"() {
    given:
    def ownerId = UUID.randomUUID()
    persistFile("exports/paged-combined/01.csv", ownerId)
    persistFile("exports/paged-combined/02.csv", ownerId)
    persistFile("exports/paged-combined/03.csv", ownerId)
    persistFile("exports/other/01.csv", ownerId)
    persistFile("exports/paged-combined/other-user.csv", UUID.randomUUID())

    when:
    def result = mockMvc.perform(get(FILES_ENDPOINT)
        .param("ownerId", ownerId.toString())
        .param("prefix", "exports/paged-combined/")
        .param("limit", "1")
        .param("offset", "1")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isOk())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response*.key == [
      "exports/paged-combined/02.csv"
    ]
    response.every { it.ownerId == ownerId.toString() }
  }

  def "should return validation error when paging params are out of range"() {
    when:
    def result = mockMvc.perform(get(FILES_ENDPOINT)
        .param(paramName, paramValue)
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isBadRequest())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response.type == "VALIDATION"

    where:
    paramName | paramValue
    "limit"  | "1001"
    "limit"  | "-1"
    "offset" | "-1"
  }

  def "should get file by id from database"() {
    given:
    def file = persistFile("exports/single/report.csv")

    when:
    def result = mockMvc.perform(get("${FILES_ENDPOINT}/${file.id}")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isOk())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response.fileId == file.id.toString()
    response.ownerId == file.ownerId.toString()
    response.key == file.key
    response.name == file.name
    response.location == file.location
    response.contentType == file.contentType
    response.size == file.size
    response.eTag == file.eTag
    response.status == file.status
    response.lastModified
  }

  def "should return not found when file does not exist"() {
    when:
    def result = mockMvc.perform(get("${FILES_ENDPOINT}/${UUID.randomUUID()}")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isNotFound())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response.type == "NOT_FOUND"
    response.message == "File not found"
  }

  def "should sync database to s3"() {
    given:
    def staleFile = persistFile("exports/stale/report.csv")
    putObject("exports/sync/report.csv", "report")
    putObject("exports/sync/archive.csv", "archive")

    when:
    def result = mockMvc.perform(get("${FILES_ENDPOINT}/sync")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isOk())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response*.key.contains("exports/sync/report.csv")
    response*.key.contains("exports/sync/archive.csv")
    !fileRepository.findById(staleFile.id).isPresent()

    and:
    def report = fileRepository.findByKey("exports/sync/report.csv").orElseThrow()
    report.id
    report.size == 6
    report.lastModified
    report.eTag
    !report.eTag.contains('"')

    and:
    def syncedResponse = response.find { it.key == "exports/sync/report.csv" }
    syncedResponse.fileId == report.id.toString()
    syncedResponse.ownerId == null
    syncedResponse.name == null
    syncedResponse.location == null
    syncedResponse.contentType == null
    syncedResponse.status == null
  }

  def "should update changed metadata during sync"() {
    given:
    putObject("exports/update/report.csv", "old")

    and:
    mockMvc.perform(get("${FILES_ENDPOINT}/sync")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isOk())

    def syncedFile = fileRepository.findByKey("exports/update/report.csv").orElseThrow()

    when:
    putObject("exports/update/report.csv", "new content")

    and:
    mockMvc.perform(get("${FILES_ENDPOINT}/sync")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isOk())

    def updatedFile = fileRepository.findByKey("exports/update/report.csv").orElseThrow()

    then:
    updatedFile.id == syncedFile.id
    updatedFile.size == 11
    updatedFile.eTag != syncedFile.eTag
  }

  private void putObject(String key, String content) {
    s3Client.putObject({
      it.bucket(Initializer.FILESERVER_BUCKET_NAME).key(key)
    }, RequestBody.fromString(content))
  }

  private FileEntity persistFile(String key) {
    persistFile(key, UUID.randomUUID())
  }

  private FileEntity persistFile(String key, UUID ownerId) {
    def fileName = key.substring(key.lastIndexOf("/") + 1)
    def location = key.substring(0, key.length() - fileName.length())

    fileRepository.save(FileEntity.builder()
        .id(UUID.randomUUID())
        .ownerId(ownerId)
        .key(key)
        .name(fileName)
        .location(location)
        .contentType("text/csv")
        .size(1024L)
        .lastModified(OffsetDateTime.parse("2026-05-15T10:00:00Z"))
        .eTag(UUID.randomUUID().toString())
        .status("UPLOADED")
        .build())
  }

  private static Object parseJson(String json) {
    new JsonSlurper().parseText(json)
  }
}
