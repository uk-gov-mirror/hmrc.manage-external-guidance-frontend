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

// $COVERAGE-OFF$

package filters

import org.apache.pekko.stream.Materializer
import com.google.inject.Inject
import javax.inject.Singleton
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.ServiceUnavailable
import play.api.mvc.*
import play.api.{Configuration, Logger}

import scala.concurrent.Future

@Singleton
class ShutterFilter @Inject() (configuration: Configuration, val messagesApi: MessagesApi, view: views.html.service_unavailable)(using val mat: Materializer)
    extends Filter
    with I18nSupport {

  override def apply(next: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] = {

    given request: Request[_] = Request(rh, "")

    val logger = Logger(getClass)
    val shuttered: Boolean = configuration.get[Boolean]("shuttered")
    val notAnAssetsRequest = !rh.path.startsWith("/external-guidance/assets/")
    if (shuttered && notAnAssetsRequest) {
      logger.warn(s"Returned a shuttered response for ${rh.path}")
      Future.successful(ServiceUnavailable(view()))
    } else {
      next(rh)
    }
  }
}

// $COVERAGE-ON$
