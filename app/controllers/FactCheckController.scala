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

package controllers

import config.ErrorHandler
import controllers.actions.FactCheckerAction
import models.errors.{DuplicateKeyError, MalformedResponseError, NotFoundError, StaleDataError}
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.*
import services.ReviewService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.{duplicate_process_code_error, fact_check_content_review}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{Future, ExecutionContext}

@Singleton
class FactCheckController @Inject() (
    errorHandler: ErrorHandler,
    factCheckerAction: FactCheckerAction,
    view: fact_check_content_review,
    duplicate_process_code_error: duplicate_process_code_error,
    reviewService: ReviewService,
    mcc: MessagesControllerComponents
) extends FrontendController(mcc)
    with I18nSupport {

  val logger: Logger = Logger(getClass)
  given ec: ExecutionContext = mcc.executionContext

  def approval(id: String): Action[AnyContent] = factCheckerAction.async { request =>
    given Request[AnyContent] = request
    reviewService.approvalFactCheck(id).flatMap {
      case Right(approvalProcessReview) => Future.successful(Ok(view(approvalProcessReview)))
      case Left(NotFoundError) =>
        logger.error(s"Unable to retrieve approval fact check for process $id")
        errorHandler.notFoundTemplate.map(NotFound(_))
      case Left(DuplicateKeyError) =>
        logger.error(s"Duplicate process code found when attempting to fact check process $id")
        Future.successful(BadRequest(duplicate_process_code_error()))
      case Left(StaleDataError) =>
        logger.error(s"The requested approval fact check for process $id can no longer be found")
        errorHandler.notFoundTemplate.map(NotFound(_))
      case Left(MalformedResponseError) =>
        logger.error(s"A malformed response was returned for the approval fact check review for process $id")
        errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
      case Left(err) =>
        // Handle stale data, internal server and any unexpected errors
        logger.error(s"Request for approval fact check for process $id returned error $err")
        errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
    }

  }
}
