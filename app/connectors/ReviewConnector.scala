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
import connectors.httpParsers.ReviewHttpParser.given 
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import models.audit.AuditInfo
import models.{ApprovalProcessReview, ApprovalProcessStatusChange, PageReviewDetail, RequestOutcome}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.StringContextOps
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReviewConnector @Inject() (httpClient: HttpClientV2, appConfig: AppConfig) {

  def approval2iReview(id: String)(using ec: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[ApprovalProcessReview]] = {

    val reviewEndPoint: String = s"${appConfig.externalGuidanceBaseUrl}/external-guidance/approval/$id/2i-review"

    httpClient.get(url"$reviewEndPoint").execute[RequestOutcome[ApprovalProcessReview]]
  }

  def approval2iReviewConfirmCheck(id: String)(using ec: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[Unit]] = {

    val reviewEndPoint: String = s"${appConfig.externalGuidanceBaseUrl}/external-guidance/approval/$id/2i-review/confirm"

    httpClient.get(url"$reviewEndPoint").execute[RequestOutcome[Unit]]
  }

  def approval2iReviewComplete(
      id: String,
      info: ApprovalProcessStatusChange
  )(using ec: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[AuditInfo]] = {

    val reviewEndPoint: String = s"${appConfig.externalGuidanceBaseUrl}/external-guidance/approval/$id/2i-review"

    httpClient.post(url"$reviewEndPoint").withBody(Json.toJson(info)).execute[RequestOutcome[AuditInfo]]
  }

  def approval2iReviewPageInfo(id: String, pageUrl: String)(using ec: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[PageReviewDetail]] = {

    val reviewEndPoint: String = s"${appConfig.externalGuidanceBaseUrl}/external-guidance/approval/$id/2i-page-review$pageUrl"

    httpClient.get(url"$reviewEndPoint").execute[RequestOutcome[PageReviewDetail]]
  }

  def approval2iReviewPageComplete(
      id: String,
      pageUrl: String,
      info: PageReviewDetail
  )(using ec: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[Unit]] = {

    val reviewEndPoint: String = s"${appConfig.externalGuidanceBaseUrl}/external-guidance/approval/$id/2i-page-review$pageUrl"

    httpClient.post(url"$reviewEndPoint").withBody(Json.toJson(info)).execute[RequestOutcome[Unit]]
  }

  def approvalFactCheck(id: String)(using ec: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[ApprovalProcessReview]] = {

    val reviewEndPoint: String = s"${appConfig.externalGuidanceBaseUrl}/external-guidance/approval/$id/fact-check"

    httpClient.get(url"$reviewEndPoint").execute[RequestOutcome[ApprovalProcessReview]]
  }

  def approvalFactCheckComplete(
      id: String,
      info: ApprovalProcessStatusChange
  )(using ec: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[AuditInfo]] = {

    val reviewEndPoint: String = s"${appConfig.externalGuidanceBaseUrl}/external-guidance/approval/$id/fact-check"

    httpClient.post(url"$reviewEndPoint").withBody(Json.toJson(info)).execute[RequestOutcome[AuditInfo]]
  }

  def factCheckPageInfo(id: String, pageUrl: String)(using ec: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[PageReviewDetail]] = {

    val reviewEndPoint: String = s"${appConfig.externalGuidanceBaseUrl}/external-guidance/approval/$id/fact-check-page-review$pageUrl"

    httpClient.get(url"$reviewEndPoint").execute[RequestOutcome[PageReviewDetail]]
  }

  def factCheckPageComplete(id: String, pageUrl: String, info: PageReviewDetail)(
      using ec: ExecutionContext,
      hc: HeaderCarrier
  ): Future[RequestOutcome[Unit]] = {

    val reviewEndPoint: String = s"${appConfig.externalGuidanceBaseUrl}/external-guidance/approval/$id/fact-check-page-review$pageUrl"

    httpClient.post(url"$reviewEndPoint").withBody(Json.toJson(info)).execute[RequestOutcome[Unit]]
  }

}
