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

import java.net.URLEncoder
import javax.inject.{Inject, Singleton}
import models.admin.CachedProcessSummary
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import config.AppConfig
import models.RequestOutcome
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.StringContextOps

@Singleton
class ViewerConnector @Inject() (httpClient: HttpClientV2, appConfig: AppConfig) {

  def listActive()(using ec: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[List[CachedProcessSummary]]] = {
    val endPoint: String = appConfig.activeProcessesUrl
    import connectors.httpParsers.ActiveProcessHttpParser.summaryHttpReads

    httpClient.get(url"$endPoint").execute[RequestOutcome[List[CachedProcessSummary]]]
  }

  def get(id: String, version: Long, timescalesVersion: Option[Long], ratesVersion: Option[Long])
         (using ec: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[JsValue]] = {
    val args: Seq[(String, String)] = Seq(timescalesVersion.map(ts => ("timescalesVersion", ts.toString)), 
                                          ratesVersion.map(rts => ("ratesVersion", rts.toString))).flatten
    val endPoint: String = appConfig.activeProcessesUrl + s"/$id/${version.toString}"
    import connectors.httpParsers.ActiveProcessHttpParser.processHttpReads

    httpClient.get(url"$endPoint${makeQueryString(args)}").execute[RequestOutcome[JsValue]]
  }

  private[connectors] def makeQueryString(queryParams: Seq[(String, String)]): String = {
    val paramPairs = queryParams.map { case (k, v) => s"$k=${URLEncoder.encode(v, "utf-8")}" }
    if (paramPairs.isEmpty) "" else paramPairs.mkString("?", "&", "")
  }

}
