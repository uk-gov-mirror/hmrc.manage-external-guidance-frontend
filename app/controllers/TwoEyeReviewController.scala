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
import controllers.actions.TwoEyeReviewerAction
import models.ApprovalStatus
import models.audit.{PublishedEvent, TwoEyeReviewCompleteEvent}
import models.errors.*
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.*
import services.{AuditService, ReviewService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.*
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TwoEyeReviewController @Inject() (
    errorHandler: ErrorHandler,
    twoEyeReviewerAction: TwoEyeReviewerAction,
    view: twoeye_content_review,
    duplicate_process_code_error: duplicate_process_code_error,
    confirmation_view: twoeye_published,
    errorView: twoeye_confirm_error,
    auditService: AuditService,
    reviewService: ReviewService,
    mcc: MessagesControllerComponents
) extends FrontendController(mcc)
    with I18nSupport {

  val logger: Logger = Logger(getClass)
  given ec: ExecutionContext = mcc.executionContext

  def approval(id: String): Action[AnyContent] = twoEyeReviewerAction.async { request =>
    given Request[AnyContent] = request
    reviewService.approval2iReview(id).flatMap {
      case Right(approvalProcessReview) =>
        Future.successful(Ok(view(approvalProcessReview)))
      case Left(NotFoundError) =>
        logger.error(s"Unable to retrieve approval 2i review for process $id")
        errorHandler.notFoundTemplate.map(NotFound(_))
      case Left(StaleDataError) =>
        logger.error(s"The requested approval 2i review for process $id can no longer be found")
        errorHandler.notFoundTemplate.map(NotFound(_))
      case Left(DuplicateKeyError) =>
        logger.error(s"Attempt to review a duplicate process code for process $id")
        Future.successful(BadRequest(duplicate_process_code_error()))
      case Left(MalformedResponseError) =>
        logger.error(s"A malformed response was returned for the approval 2i process review for process $id")
        errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
      case Left(err) =>
        // Handle stale data, internal server and any unexpected errors
        logger.error(s"Request for approval 2i review process for process $id returned error $err")
        errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
    }

  }

  def onSubmit(processId: String): Action[AnyContent] = twoEyeReviewerAction.async { request =>
    given Request[AnyContent] = request
    reviewService.approval2iReviewCheck(processId) flatMap {
      case Right(()) =>
          reviewService.approval2iReviewComplete(processId, request.credId, request.name, ApprovalStatus.Published).flatMap {
            case Right(auditInfo) =>
              Future.sequence(List(auditService.audit(TwoEyeReviewCompleteEvent(auditInfo)),
                                   auditService.audit(PublishedEvent(auditInfo)))).map(_ =>
                Ok(confirmation_view()))
            case Left(NotFoundError) =>
              logger.error(s"Unable to retrieve approval 2i review for process $processId")
              errorHandler.notFoundTemplate.map(NotFound(_))
            case Left(StaleDataError) =>
              logger.error(s"The requested approval 2i review for process $processId can no longer be found")
              errorHandler.notFoundTemplate.map(NotFound(_))
            case Left(DuplicateKeyError) =>
              logger.error(s"Attempt to publish a duplicate process code for process $processId - publish failed")
              Future.successful(BadRequest(duplicate_process_code_error()))
            case Left(err) =>
              // Handle stale data, internal server and any unexpected errors
              logger.error(s"Request for approval 2i review process for process $processId returned error $err")
              errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
          }
      case Left(IncompleteDataError) => Future.successful(BadRequest(errorView(processId)))
      case Left(NotFoundError) =>
        logger.error(s"Unable to check 2i review approval for process $processId not found")
        errorHandler.notFoundTemplate.map(NotFound(_))
      case Left(StaleDataError) =>
        logger.error(s"The requested 2i review approval check for process $processId failed - stale data")
        errorHandler.notFoundTemplate.map(NotFound(_))
      case Left(err) =>
        // Handle internal server and any unexpected errors
        logger.error(s"Request for approval 2i review process for process $processId returned error $err")
        errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
    }
  }


}
