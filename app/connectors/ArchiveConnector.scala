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

package connectors

import config.AppConfig
import models.errors.BadRequestError
import models.{ProcessSummary, RequestOutcome}
import play.api.Logger
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import play.api.libs.json.JsValue
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.StringContextOps

@Singleton
class ArchiveConnector @Inject()(httpClient: HttpClientV2, appConfig: AppConfig) {
  val logger: Logger = Logger(getClass)

  def summaries(using ec: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[List[ProcessSummary]]] = {
    val summaryEndPoint: String = appConfig.externalGuidanceBaseUrl + "/external-guidance/archived"

    import connectors.httpParsers.PublishedProcessHttpParser.processSummaryHttpReads

    httpClient.get(url"$summaryEndPoint").execute[RequestOutcome[List[ProcessSummary]]]
  }

  def archive(id: String)(using ec: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[Boolean]] = {
    val archiveEndPoint: String = appConfig.externalGuidanceBaseUrl + s"/external-guidance/archive/$id"

    import uk.gov.hmrc.http.HttpReads.Implicits.readRaw

    httpClient.get(url"$archiveEndPoint").execute[HttpResponse].map{ _ => Right(true)}
      .recover {
        case ex =>
          logger.error(s"Error archiving provess $id with exception: ${ex.getMessage}")
          Left(BadRequestError)
      }
  }

  def getArchivedById(id: String)(using ec: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[JsValue]] = {
    val publishedEndPoint: String = appConfig.externalGuidanceBaseUrl + s"/external-guidance/archived/$id"

    import connectors.httpParsers.PublishedProcessHttpParser.processHttpReads

    httpClient.get(url"$publishedEndPoint").execute[RequestOutcome[JsValue]]
  }

}
