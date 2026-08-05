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

import connectors.PublishedConnector
import models.{ProcessSummary, PublishedProcess, RequestOutcome}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import uk.gov.hmrc.http.HeaderCarrier
import play.api.libs.json.JsValue

import scala.concurrent.{ExecutionContext, Future}

trait MockPublishedConnector extends TestSuite with MockFactory {

  val mockPublishedConnector: PublishedConnector = mock[PublishedConnector]

  object MockPublishedConnector {

    def summaries: CallHandler[Future[RequestOutcome[List[ProcessSummary]]]] =
      (mockPublishedConnector
        .summaries(using _: ExecutionContext, _: HeaderCarrier))
        .expects(*, *)

    def getPublished(id: String): CallHandler[Future[RequestOutcome[PublishedProcess]]] = {
      (mockPublishedConnector
        .getPublished(_: String)(using _: ExecutionContext, _: HeaderCarrier))
        .expects(id, *, *)
    }

    def getPublishedByProcessCode(code: String): CallHandler[Future[RequestOutcome[JsValue]]] = {
      (mockPublishedConnector
        .getPublishedByProcessCode(_: String)(using _: ExecutionContext, _: HeaderCarrier))
        .expects(code, *, *)
    }

  }

}
