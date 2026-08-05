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
import mocks.MockApprovalConnector
import models.errors.InternalServerError
import models.{ApprovalResponse, RequestOutcome}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import models.ApprovalProcessSummary

class ApprovalServiceSpec extends BaseSpec {

  private trait Test extends MockApprovalConnector {

    given headerCarrier: HeaderCarrier = HeaderCarrier()

    lazy val service: ApprovalService = new ApprovalService(mockApprovalConnector)

    val processId: String = "abc12345"
    val dummyProcess: JsValue = Json.obj("meta" -> Json.obj("id" -> processId))
  }

  "The approvalProcess service" should {

    "Return an instance of the class ApprovalResponse after a successful call to the 2i Review connector" in new Test {

      MockApprovalConnector
        .submitFor2iReview(dummyProcess)
        .returns(Future.successful(Right(ApprovalResponse(processId))))

      val result: Future[RequestOutcome[ApprovalResponse]] = service.submitFor2iReview(dummyProcess)

      result.onComplete {
        case Success(response) =>
          response match {
            case Right(response) => response.id shouldBe processId
            case Left(error) => fail(s"Unexpected error returned by approvalProcess connector : ${error.toString}")
          }
        case Failure(exception) => fail(s"Future onComplete returned unexpected error : ${exception.getMessage}")
      }

    }

    "Return an error after an unsuccessful call to the 2i review connector" in new Test {

      MockApprovalConnector
        .submitFor2iReview(dummyProcess)
        .returns(Future.successful(Left(InternalServerError)))

      val result: Future[RequestOutcome[ApprovalResponse]] = service.submitFor2iReview(dummyProcess)

      result.onComplete {
        case Success(response) =>
          response match {
            case Right(_) => fail("Approval response returned when an error was expected")
            case Left(error) => error shouldBe InternalServerError
          }
        case Failure(exception) => fail(s"Future onComplete returned unexpected error : ${exception.getMessage}")
      }
    }

    "Return an instance of the class ApprovalResponse after a successful call to the fact check connector" in new Test {

      MockApprovalConnector
        .submitForFactCheck(dummyProcess)
        .returns(Future.successful(Right(ApprovalResponse(processId))))

      val result: Future[RequestOutcome[ApprovalResponse]] = service.submitForFactCheck(dummyProcess)

      result.onComplete {
        case Success(response) =>
          response match {
            case Right(response) => response.id shouldBe processId
            case Left(error) => fail(s"Unexpected error returned by approvalProcess connector : ${error.toString}")
          }
        case Failure(exception) => fail(s"Future onComplete returned unexpected error : ${exception.getMessage}")
      }

    }

    "Return an error after an unsuccessful call to the fact check connector" in new Test {

      MockApprovalConnector
        .submitForFactCheck(dummyProcess)
        .returns(Future.successful(Left(InternalServerError)))

      val result: Future[RequestOutcome[ApprovalResponse]] = service.submitForFactCheck(dummyProcess)

      result.onComplete {
        case Success(response) =>
          response match {
            case Right(_) => fail("Approval response returned when an error was expected")
            case Left(error) => error shouldBe InternalServerError
          }
        case Failure(exception) => fail(s"Future onComplete returned unexpected error : ${exception.getMessage}")
      }
    }

    "Return a list of Processes after an successful call to the summaries connector" in new Test {

      MockApprovalConnector.approvalSummaries
        .returns(Future.successful(Right(List())))

      val result: Future[RequestOutcome[List[ApprovalProcessSummary]]] = service.approvalSummaries

      result.onComplete {
        case Success(response) =>
          response match {
            case Right(_) => succeed
            case Left(err) => fail(s"Unexpected error returned by approvalSummaries connector : ${err.toString}")
          }
        case Failure(exception) => fail(s"Call failed and returned unexpected error : ${exception.getMessage}")
      }
    }

    "Return an error after an unsuccessful call to the connector by approvalSummaries" in new Test {

      MockApprovalConnector.approvalSummaries
        .returns(Future.successful(Left(InternalServerError)))

      val result: Future[RequestOutcome[List[ApprovalProcessSummary]]] = service.approvalSummaries

      result.onComplete {
        case Success(response) =>
          response match {
            case Right(_) => fail("Response returned when an error was expected")
            case Left(error) => error shouldBe InternalServerError
          }
        case Failure(exception) => fail(s"Call failed and returned unexpected error : ${exception.getMessage}")
      }
    }

  }
}
