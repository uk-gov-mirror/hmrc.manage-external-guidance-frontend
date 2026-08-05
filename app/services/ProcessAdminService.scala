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

import connectors.{PublishedConnector, ApprovalConnector, ArchiveConnector}
import javax.inject.{Inject, Singleton}
import models.{RequestOutcome, ProcessSummary}
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.HeaderCarrier
import play.api.Logger
import models.admin.CachedProcessSummary
import scala.concurrent.{ExecutionContext, Future}
import connectors.ViewerConnector

@Singleton
class ProcessAdminService @Inject()(
  published: PublishedConnector,
  approvals: ApprovalConnector,
  archived: ArchiveConnector,
  viewer: ViewerConnector) {

  val logger = Logger(getClass)

  def publishedSummaries(using context: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[List[ProcessSummary]]] =
    published.summaries

  def approvalSummaries(using context: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[List[ProcessSummary]]] =
    approvals.summaries

  def archivedSummaries(using context: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[List[ProcessSummary]]] =
    archived.summaries

  def getPublishedByProcessCode(code: String)(using ec: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[JsValue]] =
    published.getPublishedByProcessCode(code)

  def getApprovalByProcessCode(code: String)(using ec: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[JsValue]] =
    approvals.getApprovalByProcessCode(code)

  def getArchivedById(id: String)(using ec: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[JsValue]] =
    archived.getArchivedById(id)

  def activeSummaries(using ec: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[List[CachedProcessSummary]]] = viewer.listActive()

  def getActive(id: String, version: Long, timescalesVersion: Option[Long], ratesVersion: Option[Long])(using ec: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[JsValue]] = 
    viewer.get(id, version, timescalesVersion, ratesVersion)

}
