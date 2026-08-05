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

import connectors.ArchiveConnector
import models.{ProcessSummary, RequestOutcome}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import uk.gov.hmrc.http.HeaderCarrier
import play.api.libs.json.JsValue

import scala.concurrent.{ExecutionContext, Future}

trait MockArchiveConnector extends TestSuite with MockFactory {

  val mockArchiveConnector: ArchiveConnector = mock[ArchiveConnector]

  object MockArchiveConnector {

    def archive(id: String): CallHandler[Future[RequestOutcome[Boolean]]] =
      (mockArchiveConnector
        .archive(_: String)(using _: ExecutionContext, _: HeaderCarrier))
        .expects(id, *, *)

    def summaries: CallHandler[Future[RequestOutcome[List[ProcessSummary]]]] =
      (mockArchiveConnector
        .summaries(using _: ExecutionContext, _: HeaderCarrier))
        .expects(*, *)


    def getArchivedById(id: String): CallHandler[Future[RequestOutcome[JsValue]]] =
      (mockArchiveConnector
        .getArchivedById(_: String)(using _: ExecutionContext, _: HeaderCarrier))
        .expects(id, *, *)

  }

}
