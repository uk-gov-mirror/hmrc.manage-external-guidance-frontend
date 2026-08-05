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

package controllers.actions

import models.requests.IdentifierRequest
import play.api.mvc.Results.*
import play.api.mvc.*
import play.api.Logger
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Name, ~}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}
import config.AppConfig
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler

trait IdentifierAction extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]

trait PrivilegedAction extends AuthorisedFunctions{
  val predicate: Predicate
  given executionContext: ExecutionContext
  val logger: Logger = Logger(getClass)
  val appConfig: AppConfig
  val errorHandler: FrontendErrorHandler
  val continueUrl: String = appConfig.continueUrl

  def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    given hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    val unauthorizedResult =
      errorHandler.standardErrorTemplate(
        "error.unauthorized401.pageTitle.page",
        "error.unauthorized401.heading.page",
        "error.unauthorized401.message"
      )(request).map(Unauthorized(_))


    authorised(predicate)
      .retrieve(Retrievals.credentials and Retrievals.name and Retrievals.email) {
        case Some(Credentials(providerId, _)) ~ Some(Name(Some(name), _)) ~ Some(email) =>
          block(IdentifierRequest(request, providerId, name, email))
        case _ =>
          logger.error(s"${getClass.getSimpleName()} action could not retrieve required user details in method invokeBlock")
          unauthorizedResult
      }.recoverWith {
      case _: NoActiveSession =>
        Future.successful(Redirect(appConfig.loginUrl, Map("successURL" -> Seq(continueUrl))))
      case authEx: AuthorisationException =>
        logger.error(s"Method invokeBlock of ${getClass.getSimpleName()} action received an authorization exception with the message ${authEx.getMessage}")
        unauthorizedResult
    }
  }
}
