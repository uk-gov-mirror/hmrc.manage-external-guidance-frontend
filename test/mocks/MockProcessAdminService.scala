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

import models.RequestOutcome
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.JsValue
import services.ProcessAdminService
import uk.gov.hmrc.http.HeaderCarrier
import models.ProcessSummary
import models.admin.CachedProcessSummary
import org.scalatest.TestSuite

import scala.concurrent.{ExecutionContext, Future}

trait MockProcessAdminService extends TestSuite with MockFactory {

  val mockProcessAdminService: ProcessAdminService = mock[ProcessAdminService]

  object MockProcessAdminService {

    def publishedSummaries: CallHandler[Future[RequestOutcome[List[ProcessSummary]]]] =
      (mockProcessAdminService
        .publishedSummaries(using _: ExecutionContext, _: HeaderCarrier))
        .expects(*, *)

    def approvalSummaries: CallHandler[Future[RequestOutcome[List[ProcessSummary]]]] =
      (mockProcessAdminService
        .approvalSummaries(using _: ExecutionContext, _: HeaderCarrier))
        .expects(*, *)

    def archivedSummaries: CallHandler[Future[RequestOutcome[List[ProcessSummary]]]] =
      (mockProcessAdminService
        .archivedSummaries(using _: ExecutionContext, _: HeaderCarrier))
        .expects(*, *)

    def activeSummaries: CallHandler[Future[RequestOutcome[List[CachedProcessSummary]]]] =
      (mockProcessAdminService
        .activeSummaries(using _: ExecutionContext, _: HeaderCarrier))
        .expects(*, *)

    def getPublishedByProcessCode(code: String): CallHandler[Future[RequestOutcome[JsValue]]] =
      (mockProcessAdminService
        .getPublishedByProcessCode(_: String)(using _: ExecutionContext, _: HeaderCarrier))
        .expects(code, *, *)

    def getApprovalByProcessCode(code: String): CallHandler[Future[RequestOutcome[JsValue]]] =
      (mockProcessAdminService
        .getApprovalByProcessCode(_: String)(using _: ExecutionContext, _: HeaderCarrier))
        .expects(code, *, *)

    def getArchivedById(id: String): CallHandler[Future[RequestOutcome[JsValue]]] =
      (mockProcessAdminService
        .getArchivedById(_: String)(using _: ExecutionContext, _: HeaderCarrier))
        .expects(id, *, *)
    
    def getActive(id: String, version: Long, timescalesVersion: Option[Long], ratesVersion: Option[Long]): CallHandler[Future[RequestOutcome[JsValue]]] =
      (mockProcessAdminService
        .getActive(_: String, _: Long, _: Option[Long], _: Option[Long])(using _: ExecutionContext, _: HeaderCarrier))
        .expects(id, version, timescalesVersion, ratesVersion, *, *)

  }

}
