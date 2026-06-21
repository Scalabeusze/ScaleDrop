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

package com.scaledrop.sddownload.adapter.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.scaledrop.sddownload.IntegrationTestBase
import com.scaledrop.sddownload.adapter.db.FileDownloadEntity
import com.scaledrop.sddownload.adapter.db.FileDownloadRepository
import com.scaledrop.sddownload.adapter.db.FileEntity
import com.scaledrop.sddownload.adapter.db.FileRepository
import com.scaledrop.sddownload.adapter.db.FileShareEntity
import com.scaledrop.sddownload.adapter.db.FileShareRepository
import com.scaledrop.sddownload.adapter.event.listener.FileUpdatesListener
import com.scaledrop.sddownload.configuration.exception.FileUpdateEventException
import com.scaledrop.sddownload.utilities.Initializer
import java.time.OffsetDateTime
import org.springframework.beans.factory.annotation.Autowired
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import spock.util.concurrent.PollingConditions

class FileUpdatesListenerTest extends IntegrationTestBase {

  @Autowired
  SqsAsyncClient sqsAsyncClient

  @Autowired
  FileRepository fileRepository

  @Autowired
  FileDownloadRepository fileDownloadRepository

  @Autowired
  FileShareRepository fileShareRepository

  @Autowired
  ObjectMapper objectMapper

  @Autowired
  FileUpdatesListener fileUpdatesListener

  PollingConditions conditions = new PollingConditions(timeout: 8)

  def setup() {
    fileShareRepository.deleteAll()
    fileDownloadRepository.deleteAll()
    fileRepository.deleteAll()
  }

  def "should create file row from raw upload metadata event"() {
    given:
    def fileId = UUID.randomUUID()
    def ownerId = UUID.randomUUID()

    when:
    sendMessage(toJson(uploadEvent(fileId, ownerId, "report.csv", "/exports/", "text/csv", 123L, "hash-1", "UPLOADED", "FILE")))

    then:
    conditions.eventually {
      def file = fileRepository.findById(fileId).orElseThrow()
      assert file.key == "${ownerId}/${fileId}"
      assert file.ownerId == ownerId
      assert file.name == "report.csv"
      assert file.location == "/exports/"
      assert file.contentType == "text/csv"
      assert file.size == 123L
      assert file.eTag == "hash-1"
      assert file.status == "UPLOADED"
      assert file.lastModified.toInstant().toString() == "2022-10-10T15:00:00Z"
    }
  }

  def "should create file row from sns envelope event"() {
    given:
    def fileId = UUID.randomUUID()
    def ownerId = UUID.randomUUID()
    def event = uploadEvent(fileId, ownerId, "archive.zip", "exports/", "application/zip", 456L, "hash-2", "UPLOADED", "FILE")
    def envelope = [
      Type   : "Notification",
      Message: toJson(event)
    ]

    when:
    sendMessage(toJson(envelope))

    then:
    conditions.eventually {
      def file = fileRepository.findById(fileId).orElseThrow()
      assert file.key == "${ownerId}/${fileId}"
      assert file.size == 456L
      assert file.eTag == "hash-2"
    }
  }

  def "should update existing row for duplicate event"() {
    given:
    def fileId = UUID.randomUUID()
    sendMessage(toJson(uploadEvent(fileId, UUID.randomUUID(), "report.csv", "/exports/", "text/csv", 123L, "hash-1", "UPLOADED", "FILE")))

    and:
    conditions.eventually {
      assert fileRepository.findById(fileId).orElseThrow().size == 123L
    }

    when:
    sendMessage(toJson(uploadEvent(fileId, UUID.randomUUID(), "report.csv", "/exports/", "text/csv", 789L, "hash-2", "UPLOADED", "FILE")))

    then:
    conditions.eventually {
      assert fileRepository.findAll().size() == 1
      def file = fileRepository.findById(fileId).orElseThrow()
      assert file.size == 789L
      assert file.eTag == "hash-2"
    }
  }

  def "should reconcile same key row to upload file id"() {
    given:
    def ownerId = UUID.randomUUID()
    def uploadFileId = UUID.randomUUID()
    def syncFile = persistFile("${ownerId}/${uploadFileId}")

    when:
    sendMessage(toJson(uploadEvent(uploadFileId, ownerId, "report.csv", "/exports/", "text/csv", 123L, "hash-1", "UPLOADED", "FILE")))

    then:
    conditions.eventually {
      assert !fileRepository.findById(syncFile.id).isPresent()
      def file = fileRepository.findById(uploadFileId).orElseThrow()
      assert file.key == "${ownerId}/${uploadFileId}"
      assert file.eTag == "hash-1"
    }
  }

  def "should ignore folder and non uploaded events"() {
    when:
    sendMessage(toJson(uploadEvent(UUID.randomUUID(), UUID.randomUUID(), "folder", "/exports/", null, 0L, "hash-1", "UPLOADED", "FOLDER")))
    sendMessage(toJson(uploadEvent(UUID.randomUUID(), UUID.randomUUID(), "pending.csv", "/exports/", "text/csv", 123L, "hash-2", "PENDING", "FILE")))

    then:
    conditions.eventually {
      assert fileRepository.findAll().isEmpty()
    }
  }

  def "should mark file deleted from delete event and remove shares"() {
    given:
    def ownerId = UUID.randomUUID()
    def file = persistFile("exports/delete/report.csv", ownerId)
    persistShare(file, ownerId, UUID.randomUUID())
    persistDownload(file)

    when:
    sendMessage(toJson(uploadEvent(file.id, ownerId, "report.csv", "/exports/delete/", "text/csv", 123L, "hash-1", "DELETED", "FILE")))

    then:
    conditions.eventually {
      def deletedFile = fileRepository.findById(file.id).orElseThrow()
      assert deletedFile.status == "DELETED"
      assert fileShareRepository.findAll().isEmpty()
      assert fileDownloadRepository.findAll().size() == 1
      assert fileDownloadRepository.findAll().first().fileId == file.id
    }
  }

  def "should ignore delete event for unknown file"() {
    when:
    sendMessage(toJson(uploadEvent(UUID.randomUUID(), UUID.randomUUID(), "missing.csv", "/exports/delete/", "text/csv", 123L, "hash-1", "DELETED", "FILE")))

    then:
    conditions.eventually {
      assert fileRepository.findAll().isEmpty()
    }
  }

  def "should reject deleted file event without file id"() {
    when:
    fileUpdatesListener.onEvent(toJson([
      status: "DELETED",
      type  : "FILE"
    ]))

    then:
    def exception = thrown(FileUpdateEventException)
    exception.message == "Invalid deleted file event, missing required fields: fileId"
    fileRepository.findAll().isEmpty()
  }

  def "should ignore deleted folder event"() {
    when:
    sendMessage(toJson(uploadEvent(UUID.randomUUID(), UUID.randomUUID(), "folder", "/exports/", null, 0L, "hash-1", "DELETED", "FOLDER")))

    then:
    conditions.eventually {
      assert fileRepository.findAll().isEmpty()
    }
  }

  def "should not create row from invalid uploaded file event"() {
    when:
    fileUpdatesListener.onEvent(toJson([
      fileId: UUID.randomUUID().toString(),
      name  : "broken.csv",
      status: "UPLOADED",
      type  : "FILE"
    ]))

    then:
    def exception = thrown(FileUpdateEventException)
    exception.message == "Invalid uploaded file event, missing required fields: ownerId, size, hash"
    fileRepository.findAll().isEmpty()
  }

  def "should reject malformed file update event payload"() {
    when:
    fileUpdatesListener.onEvent("not-json")

    then:
    def exception = thrown(FileUpdateEventException)
    exception.message == "Invalid file update event payload"
  }

  private void sendMessage(String messageBody) {
    sqsAsyncClient.sendMessage {
      it.queueUrl(Initializer.fileUpdatesQueueUrlValue()).messageBody(messageBody)
    }.join()
  }

  private String toJson(Object value) {
    objectMapper.writeValueAsString(value)
  }

  private static Map uploadEvent(
      UUID fileId,
      UUID ownerId,
      String name,
      String location,
      String contentType,
      Long size,
      String hash,
      String status,
      String type) {
    [
      fileId     : fileId.toString(),
      ownerId    : ownerId.toString(),
      name       : name,
      location   : location,
      contentType: contentType,
      size       : size,
      hash       : hash,
      status     : status,
      type       : type
    ]
  }

  private FileEntity persistFile(String key) {
    persistFile(key, UUID.randomUUID())
  }

  private FileEntity persistFile(String key, UUID ownerId) {
    fileRepository.save(FileEntity.builder()
        .id(UUID.randomUUID())
        .ownerId(ownerId)
        .key(key)
        .size(1024L)
        .lastModified(OffsetDateTime.parse("2026-05-15T10:00:00Z"))
        .eTag(UUID.randomUUID().toString())
        .status("UPLOADED")
        .build())
  }

  private FileShareEntity persistShare(FileEntity file, UUID fromId, UUID toId) {
    fileShareRepository.save(FileShareEntity.builder()
        .id(UUID.randomUUID())
        .fileId(file.id)
        .fromId(fromId)
        .toId(toId)
        .build())
  }

  private FileDownloadEntity persistDownload(FileEntity file) {
    fileDownloadRepository.save(FileDownloadEntity.builder()
        .id(UUID.randomUUID())
        .fileId(file.id)
        .requestedAt(OffsetDateTime.parse("2026-05-15T10:00:00Z"))
        .expiresAt(OffsetDateTime.parse("2026-05-15T11:00:00Z"))
        .build())
  }
}
