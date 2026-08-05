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
import scala.concurrent.Future
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier
import models.{LabelledDataUpdateStatus, UpdateDetails, RequestOutcome}
import models.errors.InternalServerError
import base.BaseSpec
import mocks.MockRatesConnector
import java.time.ZonedDateTime
import scala.util.{Failure, Success}

class RatesServiceSpec extends BaseSpec {

  trait Test extends MockRatesConnector {
    given headerCarrier: HeaderCarrier = HeaderCarrier()
    lazy val ratesService: RatesService = new RatesService(mockRatesConnector)
    val dummyTimescales: JsValue = Json.parse("""{"TimescaleID": 10}""")
    val lastUpdateTime: ZonedDateTime = ZonedDateTime.of(2020, 1, 1, 12, 0, 1, 0, ZonedDateTime.now.getZone)
    val ratesJson: JsValue = Json.parse("""{"First": 1, "Second": 2, "Third": 3}""")
    val rates: Map[String, Int] = Map("First" -> 1, "Second" -> 2, "Third" -> 3)
    val credId: String = "234324234"
    val user: String = "User Blah"
    val email: String = "user@blah.com"
    val updateDetail: UpdateDetails = UpdateDetails(lastUpdateTime, "234324234", "User Blah", "user@blah.com")
    val ratesDetail: LabelledDataUpdateStatus = LabelledDataUpdateStatus(rates.size, Some(updateDetail))
  }

  "The Rates service" should {

    "Return details response after a successful rates submission" in new Test {

      MockRatesConnector
        .submitRates(dummyTimescales)
        .returns(Future.successful(Right(ratesDetail)))

      val result: Future[RequestOutcome[LabelledDataUpdateStatus]] = ratesService.submitRates(dummyTimescales)

      result.onComplete {
        case Success(response) =>
          (response: @unchecked) match {
            case Right(details) if details == ratesDetail =>  succeed
            case Left(error) => fail(s"Unexpected error returned by timescales connector : ${error.toString}")
          }

        case Failure(exception) => fail(s"Future onComplete returned unexpected error : ${exception.getMessage}")
      }

    }

    "Return details response awhen requested" in new Test {

      MockRatesConnector
        .details()
        .returns(Future.successful(Right(ratesDetail)))

      val result: Future[RequestOutcome[LabelledDataUpdateStatus]] = ratesService.details()

      result.onComplete {
        case Success(response) =>
          (response: @unchecked) match {
            case Right(details) if details == ratesDetail =>  succeed
            case Left(error) => fail(s"Unexpected error returned by timescales connector : ${error.toString}")
          }

        case Failure(exception) => fail(s"Future onComplete returned unexpected error : ${exception.getMessage}")
      }

    }

    "Return an error after an unsuccessful call by the connector" in new Test {

      MockRatesConnector
        .submitRates(dummyTimescales)
        .returns(Future.successful(Left(InternalServerError)))

      val result: Future[RequestOutcome[LabelledDataUpdateStatus]] = ratesService.submitRates(dummyTimescales)

      result.onComplete {
        case Success(response) =>
          response match {
            case Right(_) => fail("Success response returned when an error was expected")
            case Left(error) => error shouldBe InternalServerError
          }

        case Failure(exception) => fail(s"Future onComplete returned unexpected error : ${exception.getMessage}")
      }
    }
  }
}
