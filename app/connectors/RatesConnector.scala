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

import javax.inject.{Inject, Singleton}
import models.LabelledDataUpdateStatus
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json.{Json, JsValue}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import config.AppConfig
import models.RequestOutcome
import uk.gov.hmrc.http.StringContextOps

@Singleton
class RatesConnector @Inject() (httpClient: HttpClientV2, appConfig: AppConfig) {
  import connectors.httpParsers.LabelledDataHttpParser.labelledDataHttpReads

  def details()(using ec: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[LabelledDataUpdateStatus]] = {
    val endPoint: String = appConfig.externalGuidanceBaseUrl + "/external-guidance/rates"

    httpClient.get(url"$endPoint").execute[RequestOutcome[LabelledDataUpdateStatus]]
  }

  def submitRates(rates: JsValue)(using ec: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[LabelledDataUpdateStatus]] = {
    val endpoint: String = appConfig.externalGuidanceBaseUrl + "/external-guidance/rates"

    httpClient.post(url"$endpoint").withBody(Json.toJson(rates)).execute[RequestOutcome[LabelledDataUpdateStatus]]
  }

  def get()(using ec: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[JsValue]] = {
    val endPoint: String = appConfig.externalGuidanceBaseUrl + "/external-guidance/rates/data"
    import connectors.httpParsers.PublishedProcessHttpParser.processHttpReads

    httpClient.get(url"$endPoint").execute[RequestOutcome[JsValue]]
  }

}
