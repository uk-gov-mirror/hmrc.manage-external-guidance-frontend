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

import scala.concurrent.ExecutionContext.Implicits.global
import java.util.UUID.randomUUID

import scala.concurrent.Future
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier
import models.{RequestOutcome, ScratchResponse}
import models.errors.InternalServerError
import base.BaseSpec
import mocks.MockScratchConnector

import scala.util.{Failure, Success}

class ScratchServiceSpec extends BaseSpec {

  private trait Test extends MockScratchConnector {

    given headerCarrier: HeaderCarrier = HeaderCarrier()

    lazy val scratchService: ScratchService = new ScratchService(mockScratchConnector)

    val dummyProcess: JsValue = Json.parse(
      """|{
         | "processId": "11"
         |}""".stripMargin
    )

    val uuid: String = randomUUID().toString
  }

  "The scratch service" should {

    "Return an instance of the class ScratchResponse after a successful call by the connector" in new Test {

      MockScratchConnector
        .submitScratchProcess(dummyProcess)
        .returns(Future.successful(Right(ScratchResponse(uuid))))

      val result: Future[RequestOutcome[ScratchResponse]] = scratchService.submitScratchProcess(dummyProcess)

      result.onComplete {
        case Success(response) =>
          response match {
            case Right(scratchResponse) => scratchResponse.id shouldBe uuid
            case Left(error) => fail(s"Unexpected error returned by scratch connector : ${error.toString}")
          }

        case Failure(exception) => fail(s"Future onComplete returned unexpected error : ${exception.getMessage}")
      }

    }

    "Return an error after an unsuccessful call by the connector" in new Test {

      MockScratchConnector
        .submitScratchProcess(dummyProcess)
        .returns(Future.successful(Left(InternalServerError)))

      val result: Future[RequestOutcome[ScratchResponse]] = scratchService.submitScratchProcess(dummyProcess)

      result.onComplete {
        case Success(response) =>
          response match {
            case Right(_) => fail("Scratch response returned when an error was expected")
            case Left(error) => error shouldBe InternalServerError
          }

        case Failure(exception) => fail(s"Future onComplete returned unexpected error : ${exception.getMessage}")
      }
    }
  }
}
