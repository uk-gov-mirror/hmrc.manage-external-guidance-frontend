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

package mocks

import scala.concurrent.{ExecutionContext, Future}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.HeaderCarrier
import connectors.ScratchConnector
import models.{RequestOutcome, ScratchResponse}
import org.scalatest.TestSuite

trait MockScratchConnector extends TestSuite with MockFactory {

  val mockScratchConnector: ScratchConnector = mock[ScratchConnector]

  object MockScratchConnector {

    def submitScratchProcess(process: JsValue): CallHandler[Future[RequestOutcome[ScratchResponse]]] = {

      (mockScratchConnector
        .submitScratchProcess(_: JsValue)(using _: ExecutionContext, _: HeaderCarrier))
        .expects(process, *, *)
    }

  }

}
