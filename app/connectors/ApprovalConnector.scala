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
import javax.inject.{Inject, Singleton}
import models.{ApprovalProcessSummary, ApprovalResponse, RequestOutcome, ProcessSummary}
import play.api.libs.json.{Json, JsValue}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.StringContextOps

@Singleton
class ApprovalConnector @Inject() (httpClient: HttpClientV2, appConfig: AppConfig) {

  def approvalSummaries(using ec: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[List[ApprovalProcessSummary]]] = {

    import connectors.httpParsers.ApprovalHttpParser.getApprovalSummaryListHttpReads

    val summaryEndPoint: String = appConfig.externalGuidanceBaseUrl + "/external-guidance/approval"

    httpClient.get(url"$summaryEndPoint").execute[RequestOutcome[List[ApprovalProcessSummary]]]
  }

  def submitFor2iReview(process: JsValue)(using ec: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[ApprovalResponse]] = {

    import connectors.httpParsers.ApprovalHttpParser.saveApprovalHttpReads

    val endpoint: String = appConfig.externalGuidanceBaseUrl + "/external-guidance/approval/2i-review"

    httpClient.post(url"$endpoint").withBody(Json.toJson(process)).execute[RequestOutcome[ApprovalResponse]]
  }

  def submitForFactCheck(process: JsValue)(using ec: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[ApprovalResponse]] = {

    import connectors.httpParsers.ApprovalHttpParser.saveApprovalHttpReads

    val endpoint: String = appConfig.externalGuidanceBaseUrl + "/external-guidance/approval/fact-check"

    httpClient.post(url"$endpoint").withBody(Json.toJson(process)).execute[RequestOutcome[ApprovalResponse]]
  }

  def summaries(using ec: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[List[ProcessSummary]]] = {
    import connectors.httpParsers.PublishedProcessHttpParser.processSummaryHttpReads

    val endPoint: String = appConfig.externalGuidanceBaseUrl + "/external-guidance/approval/list"
    httpClient.get(url"$endPoint").execute[RequestOutcome[List[ProcessSummary]]]
  }

  def getApprovalByProcessCode(code: String)(using ec: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[JsValue]] = {
    val endPoint: String = appConfig.externalGuidanceBaseUrl + s"/external-guidance/approval/code/$code"

    import connectors.httpParsers.PublishedProcessHttpParser.processHttpReads
    httpClient.get(url"$endPoint").execute[RequestOutcome[JsValue]]
  }

}
