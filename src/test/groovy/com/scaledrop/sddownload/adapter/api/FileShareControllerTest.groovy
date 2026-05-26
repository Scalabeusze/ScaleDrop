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

import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import com.scaledrop.sddownload.WiremockTestBase
import com.scaledrop.sddownload.adapter.db.FileEntity
import com.scaledrop.sddownload.adapter.db.FileRepository
import com.scaledrop.sddownload.adapter.db.FileShareEntity
import com.scaledrop.sddownload.adapter.db.FileShareRepository
import groovy.json.JsonSlurper
import java.time.OffsetDateTime
import org.springframework.beans.factory.annotation.Autowired

class FileShareControllerTest extends WiremockTestBase {

  private static final String FILE_SHARES_ENDPOINT = "/api/v1/file-shares"

  @Autowired
  FileRepository fileRepository

  @Autowired
  FileShareRepository fileShareRepository

  def setup() {
    fileShareRepository.deleteAll()
    fileRepository.deleteAll()
  }

  def "should create file share"() {
    given:
    def ownerId = UUID.randomUUID()
    def recipientId = UUID.randomUUID()
    def file = persistFile("exports/shares/report.csv", ownerId)

    when:
    def result = mockMvc.perform(post(FILE_SHARES_ENDPOINT)
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString([
          fileId: file.id,
          fromId: ownerId,
          toId: recipientId
        ]))
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isCreated())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response.shareId != null
    response.fileId == file.id.toString()
    response.fromId == ownerId.toString()
    response.toId == recipientId.toString()

    and:
    def shares = fileShareRepository.findAll()
    shares.size() == 1
    shares.first().id.toString() == response.shareId
    shares.first().fileId == file.id
    shares.first().fromId == ownerId
    shares.first().toId == recipientId
  }

  def "should return not found when creating share for missing file"() {
    when:
    def result = mockMvc.perform(post(FILE_SHARES_ENDPOINT)
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString([
          fileId: UUID.randomUUID(),
          fromId: UUID.randomUUID(),
          toId: UUID.randomUUID()
        ]))
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isNotFound())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response.type == "NOT_FOUND"
    fileShareRepository.findAll().isEmpty()
  }

  def "should return validation error when from id does not match file owner"() {
    given:
    def file = persistFile("exports/shares/not-owner.csv", UUID.randomUUID())

    when:
    def result = mockMvc.perform(post(FILE_SHARES_ENDPOINT)
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString([
          fileId: file.id,
          fromId: UUID.randomUUID(),
          toId: UUID.randomUUID()
        ]))
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isBadRequest())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response.type == "VALIDATION"
    fileShareRepository.findAll().isEmpty()
  }

  def "should return conflict when file share already exists"() {
    given:
    def ownerId = UUID.randomUUID()
    def recipientId = UUID.randomUUID()
    def file = persistFile("exports/shares/duplicate.csv", ownerId)
    persistShare(file, ownerId, recipientId)

    when:
    def result = mockMvc.perform(post(FILE_SHARES_ENDPOINT)
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString([
          fileId: file.id,
          fromId: ownerId,
          toId: recipientId
        ]))
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isConflict())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response.type == "CONFLICT"
    fileShareRepository.findAll().size() == 1
  }

  def "should list file shares unfiltered ordered by id"() {
    given:
    def ownerId = UUID.randomUUID()
    def file = persistFile("exports/shares/list.csv", ownerId)
    def highIdShare = persistShare(file, ownerId, UUID.randomUUID(), UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"))
    def lowIdShare = persistShare(file, ownerId, UUID.randomUUID(), UUID.fromString("00000000-0000-0000-0000-000000000001"))

    when:
    def result = mockMvc.perform(get(FILE_SHARES_ENDPOINT)
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isOk())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response*.shareId == [
      lowIdShare.id.toString(),
      highIdShare.id.toString()
    ]
    response.every { it.fileId == file.id.toString() }
  }

  def "should list file shares by optional filters"() {
    given:
    def ownerId = UUID.randomUUID()
    def recipientId = UUID.randomUUID()
    def otherRecipientId = UUID.randomUUID()
    def file = persistFile("exports/shares/filter.csv", ownerId)
    def matchingShare = persistShare(file, ownerId, recipientId)
    persistShare(file, ownerId, otherRecipientId)

    when:
    def result = mockMvc.perform(get(FILE_SHARES_ENDPOINT)
        .param("fromId", ownerId.toString())
        .param("toId", recipientId.toString())
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isOk())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response*.shareId == [matchingShare.id.toString()]
    response.every { it.fromId == ownerId.toString() }
    response.every { it.toId == recipientId.toString() }
  }

  def "should list file shares by optional limit and offset"() {
    given:
    def ownerId = UUID.randomUUID()
    def file = persistFile("exports/shares/paging.csv", ownerId)
    persistShare(file, ownerId, UUID.randomUUID(), UUID.fromString("00000000-0000-0000-0000-000000000001"))
    def middleShare = persistShare(file, ownerId, UUID.randomUUID(), UUID.fromString("00000000-0000-0000-0000-000000000002"))
    persistShare(file, ownerId, UUID.randomUUID(), UUID.fromString("00000000-0000-0000-0000-000000000003"))

    when:
    def result = mockMvc.perform(get(FILE_SHARES_ENDPOINT)
        .param("limit", "1")
        .param("offset", "1")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isOk())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response*.shareId == [middleShare.id.toString()]
  }

  def "should delete file share"() {
    given:
    def ownerId = UUID.randomUUID()
    def file = persistFile("exports/shares/delete.csv", ownerId)
    def share = persistShare(file, ownerId, UUID.randomUUID())

    expect:
    mockMvc.perform(delete("${FILE_SHARES_ENDPOINT}/${share.id}")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isNoContent())

    and:
    fileShareRepository.findAll().isEmpty()
  }

  def "should return not found when deleting missing file share"() {
    when:
    def result = mockMvc.perform(delete("${FILE_SHARES_ENDPOINT}/${UUID.randomUUID()}")
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isNotFound())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response.type == "NOT_FOUND"
  }

  def "should return validation error for invalid list params"() {
    when:
    def result = mockMvc.perform(get(FILE_SHARES_ENDPOINT)
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
    "fromId" | "invalid"
    "toId"   | "invalid"
  }

  def "should return validation error when creating file share with invalid body"() {
    when:
    def result = mockMvc.perform(post(FILE_SHARES_ENDPOINT)
        .contentType(APPLICATION_JSON)
        .content(objectMapper.writeValueAsString([
          fileId: fileId,
          fromId: fromId,
          toId: toId
        ]))
        .with(httpBasic(INTERNAL_USERNAME, INTERNAL_PASSWORD)))
        .andExpect(status().isBadRequest())
        .andReturn()

    def response = parseJson(result.response.contentAsString)

    then:
    response.type == "VALIDATION"

    where:
    fileId        | fromId        | toId
    null          | UUID.randomUUID() | UUID.randomUUID()
    UUID.randomUUID() | null          | UUID.randomUUID()
    UUID.randomUUID() | UUID.randomUUID() | null
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

  private FileShareEntity persistShare(FileEntity file, UUID fromId, UUID toId) {
    persistShare(file, fromId, toId, UUID.randomUUID())
  }

  private FileShareEntity persistShare(FileEntity file, UUID fromId, UUID toId, UUID shareId) {
    fileShareRepository.save(FileShareEntity.builder()
        .id(shareId)
        .fileId(file.id)
        .fromId(fromId)
        .toId(toId)
        .build())
  }

  private static Object parseJson(String json) {
    new JsonSlurper().parseText(json)
  }
}
