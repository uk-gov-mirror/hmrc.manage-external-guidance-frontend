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

import base.BaseSpec
import mocks.{MockPublishedConnector, MockArchiveConnector, MockViewerConnector}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import models.ProcessSummary
import models.admin.CachedProcessSummary
import java.time.{Instant, ZonedDateTime}
import mocks.MockApprovalConnector

class ProcessAdminServiceSpec extends BaseSpec {

  private trait Test extends MockPublishedConnector with MockArchiveConnector with MockApprovalConnector with MockViewerConnector {

    given headerCarrier: HeaderCarrier = HeaderCarrier()

    lazy val service: ProcessAdminService = new ProcessAdminService(mockPublishedConnector, mockApprovalConnector, mockArchiveConnector, mockViewerConnector)
    val now = ZonedDateTime.now
    val processId: String = now.toInstant().toEpochMilli().toString
    val processVersion: Long = 123456789L
    val processCode: String = "code"
    val dummyProcess: JsValue = Json.obj("meta" -> Json.obj("id" -> processId))
    val process = Json.obj()
    val processSummary = ProcessSummary(processId, processCode, 1, "author", None, now, "actionedby", "Status")
    val cachedProcessSummary = CachedProcessSummary(processId, processVersion, None, None, "Title", Instant.now)
  }

  "The ProcessAdmin service" should {

    "Return an a list of published process summaries" in new Test {

      MockPublishedConnector
        .summaries
        .returns(Future.successful(Right(List(processSummary))))

      service.publishedSummaries.onComplete {
        case Success(response) =>
          response match {
            case Right(response) => response shouldBe List(processSummary)
            case Left(error) => fail(s"Unexpected error returned by published connector : ${error.toString}")
          }
        case Failure(exception) => fail(s"Future onComplete returned unexpected error : ${exception.getMessage}")
      }
    }

    "Return an a list of approval process summaries" in new Test {

      MockApprovalConnector
        .summaries
        .returns(Future.successful(Right(List(processSummary))))

      service.approvalSummaries.onComplete {
        case Success(response) =>
          response match {
            case Right(response) => response shouldBe List(processSummary)
            case Left(error) => fail(s"Unexpected error returned by approval connector : ${error.toString}")
          }
        case Failure(exception) => fail(s"Future onComplete returned unexpected error : ${exception.getMessage}")
      }
    }

    "Return an a list of archived process summaries" in new Test {

      MockArchiveConnector
        .summaries
        .returns(Future.successful(Right(List(processSummary))))

      service.archivedSummaries.onComplete {
        case Success(response) =>
          response match {
            case Right(response) => response shouldBe List(processSummary)
            case Left(error) => fail(s"Unexpected error returned by archived connector : ${error.toString}")
          }
        case Failure(exception) => fail(s"Future onComplete returned unexpected error : ${exception.getMessage}")
      }
    }

    "Return an a list of active process summaries" in new Test {

      MockViewerConnector
        .listActive
        .returns(Future.successful(Right(List(cachedProcessSummary))))

      service.activeSummaries.onComplete {
        case Success(response) =>
          response match {
            case Right(response) => response shouldBe List(cachedProcessSummary)
            case Left(error) => fail(s"Unexpected error returned by viewer connector : ${error.toString}")
          }
        case Failure(exception) => fail(s"Future onComplete returned unexpected error : ${exception.getMessage}")
      }
    }

    "Retrieve a published process by process code" in new Test {

      MockPublishedConnector
        .getPublishedByProcessCode(processCode)
        .returns(Future.successful(Right(process)))

      service.getPublishedByProcessCode(processCode).onComplete {
        case Success(response) =>
          response match {
            case Right(response) => response shouldBe process
            case Left(error) => fail(s"Unexpected error returned by published connector : ${error.toString}")
          }
        case Failure(exception) => fail(s"Future onComplete returned unexpected error : ${exception.getMessage}")
      }
    }

    "Retrieve a approval process by process code" in new Test {

      MockApprovalConnector
        .getApprovalByProcessCode(processCode)
        .returns(Future.successful(Right(process)))

      service.getApprovalByProcessCode(processCode).onComplete {
        case Success(response) =>
          response match {
            case Right(response) => response shouldBe process
            case Left(error) => fail(s"Unexpected error returned by approval connector : ${error.toString}")
          }
        case Failure(exception) => fail(s"Future onComplete returned unexpected error : ${exception.getMessage}")
      }
    }

    "Retrieve an archived process by id" in new Test {

      MockArchiveConnector
        .getArchivedById(processId)
        .returns(Future.successful(Right(process)))

      service.getArchivedById(processId).onComplete {
        case Success(response) =>
          response match {
            case Right(_) =>
            case Left(error) => fail(s"Unexpected error returned by approvalProcess connector : ${error.toString}")
          }
        case Failure(exception) => fail(s"Future onComplete returned unexpected error : ${exception.getMessage}")
      }
    }

    "Retrieve an active process by id" in new Test {

      MockViewerConnector
        .get(processId, processVersion, None, None)
        .returns(Future.successful(Right(process)))

      service.getActive(processId, processVersion, None, None).onComplete {
        case Success(response) =>
          response match {
            case Right(_) =>
            case Left(error) => fail(s"Unexpected error returned by viewer connector : ${error.toString}")
          }
        case Failure(exception) => fail(s"Future onComplete returned unexpected error : ${exception.getMessage}")
      }
    }
  }
}
