/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package services

import java.time.Instant
import java.util.UUID

import base.BaseSpec
import mocks.{MockAppConfig, MockAuditConnector}
import models.audit.{TwoEyeReviewCompleteEvent, _}
import play.api.libs.json._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import scala.concurrent.ExecutionContext.Implicits.global

class AuditServiceSpec extends BaseSpec {

  private trait Test extends MockAuditConnector {
    given headerCarrier: HeaderCarrier = HeaderCarrier()
    lazy val auditService: AuditService = new AuditService(MockAppConfig, mockAuditConnector)
    val PID: String = "SomeonePID"
    val processID: String = "ext90002"
    val processTitle: String = "A process title"
    val processVersion: Int = 1
    val ocelotVersion: Int = 3
    val ocelotLastUpdate: Long = 10000000
    val ocelotAuthor: String = "12345"
    val submissionTime: Instant = Instant.now()
    val eventUUID: String = UUID.randomUUID().toString

    def tagsData(name: String, path: Option[String] = None): Map[String, String] =
      Map(
        "clientIP" -> "-",
        "path" -> s"${path.getOrElse("-")}",
        "X-Session-ID" -> "-",
        "Akamai-Reputation" -> "-",
        "X-Request-ID" -> "-",
        "deviceID" -> "-",
        "clientPort" -> "-",
        "transactionName" -> name
      )
    val auditInfo: AuditInfo = AuditInfo(PID, processID, processTitle, processVersion, ocelotAuthor, ocelotLastUpdate, ocelotVersion)
    val twoEyeReviewCompleteEvent: TwoEyeReviewCompleteEvent = TwoEyeReviewCompleteEvent(auditInfo)
    val factCheckCompleteEvent: FactCheckCompleteEvent = FactCheckCompleteEvent(auditInfo)
    val publishedEvent: PublishedEvent = PublishedEvent(auditInfo)
  }

  "The Audit service" should {

    "Accept an TwoEyeReviewCompleteEvent object" in new Test {
      val details: JsValue = Json.toJson(twoEyeReviewCompleteEvent : AuditEvent)
      val path: Option[String] = Some("/guidance/scratch")
      val extendedEvent: ExtendedDataEvent = ExtendedDataEvent(
        "manage-external-guidance-frontend",
        "2iReviewCompleted",
        eventUUID,
        tagsData("2iReviewCompleted", path),
        details,
        submissionTime
      )
      MockAuditConnector.sendExtendedEvent(extendedEvent)

      auditService.audit(twoEyeReviewCompleteEvent, path)

    }

    "Accept a FactCheckCompleteEvent object" in new Test {
      val details: JsValue = Json.toJson(factCheckCompleteEvent : AuditEvent)
      val path: Option[String] = Some("/guidance/approve")
      val extendedEvent: ExtendedDataEvent = ExtendedDataEvent(
        "manage-external-guidance-frontend",
        "factCheckComplete",
        eventUUID,
        tagsData("factCheckComplete", path),
        details,
        submissionTime
      )
      MockAuditConnector.sendExtendedEvent(extendedEvent)

      auditService.audit(factCheckCompleteEvent, path)

    }

    "Accept an PublishedEvent object" in new Test {
      val details: JsValue = Json.toJson(publishedEvent : AuditEvent)
      val path: Option[String] = Some("/guidance/publish")
      val extendedEvent: ExtendedDataEvent =
        ExtendedDataEvent("manage-external-guidance-frontend", "published", eventUUID, tagsData("published", path), details, submissionTime)
      MockAuditConnector.sendExtendedEvent(extendedEvent)

      auditService.audit(publishedEvent, path)

    }

  }
}
