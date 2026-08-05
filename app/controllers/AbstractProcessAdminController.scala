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
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.*
import services.ProcessAdminService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.process_admin.{approval_summaries, archived_summaries, published_summaries, active_summaries}
import java.time.ZonedDateTime
import scala.concurrent.{ExecutionContext, Future}
import models.admin.navigation.AdminPage

abstract class AbstractProcessAdminController (
    errorHandler: ErrorHandler,
    publishedView: published_summaries,
    archivedView: archived_summaries,
    approvalsView: approval_summaries,
    activeView: active_summaries,
    adminService: ProcessAdminService,
    mcc: MessagesControllerComponents
) extends FrontendController(mcc)
    with I18nSupport {
  given localDateOrdering: Ordering[ZonedDateTime] = Ordering.by(_.toInstant)
  val logger: Logger = Logger(getClass)
  given ec: ExecutionContext = mcc.executionContext
  val pages: List[AdminPage]

  def published(guidanceCall: String => Call)(using request: Request[_]): Future[Result] = 
    adminService.publishedSummaries.flatMap {
      case Right(processList) => Future.successful(Ok(publishedView(processList.sortBy(_.actioned).reverse, pages, guidanceCall)))
      case Left(err) =>
        logger.error(s"Unable to retrieve list of published process summaries, err = $err")
        errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
    }

  def getPublishedGuidance(processCode: String)(using request: Request[_]): Future[Result] = 
    adminService.getPublishedByProcessCode(processCode).flatMap {
      case Right(process) => Future.successful(Ok(process))
      case Left(err) =>
        logger.error(s"Unable to retrieve published process by process code, err = $err")
        errorHandler.notFoundTemplate.map(BadRequest(_))
    }

  def approvals(guidanceCall: String => Call)(using request: Request[_]): Future[Result] = 
    adminService.approvalSummaries.flatMap {
      case Right(processList) => Future.successful(Ok(approvalsView(processList.sortBy(_.actioned).reverse, pages, guidanceCall)))
      case Left(err) =>
        logger.error(s"Unable to retrieve list of approval process summaries, err = $err")
        errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
    }

  def getApprovalGuidance(processCode: String)(using request: Request[_]): Future[Result] = 
    adminService.getApprovalByProcessCode(processCode).flatMap {
      case Right(process) => Future.successful(Ok(process))
      case Left(err) =>
        logger.error(s"Unable to retrieve published process by process code, err = $err")
        errorHandler.notFoundTemplate.map(BadRequest(_))
    }

  def archived(guidanceCall: String => Call)(using request: Request[_]): Future[Result] = 
    adminService.archivedSummaries.flatMap {
      case Right(processList) => Future.successful(Ok(archivedView(processList.sortBy(_.actioned).reverse, pages, guidanceCall)))
      case Left(err) =>
        logger.error(s"Unable to retrieve list of archived process summaries, err = $err")
        errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
    }

  def getArchivedGuidance(id: String)(using request: Request[_]): Future[Result] = 
    adminService.getArchivedById(id).flatMap {
      case Right(process) => Future.successful(Ok(process))
      case Left(err) =>
        logger.error(s"Unable to retrieve archived process by id, err = $err")
        errorHandler.notFoundTemplate.map(BadRequest(_))
    }
  
  def active(guidanceCall: (String, Long, Option[Long], Option[Long]) => Call)(using request: Request[_]): Future[Result] = 
    adminService.activeSummaries.flatMap {
      case Right(summaryList) => Future.successful(Ok(activeView(summaryList.sortBy(_.expiryTime).reverse, pages, guidanceCall)))
      case Left(err) =>
        logger.error(s"Unable to retrieve list of archived process summaries, err = $err")
        errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
    }

  def getActiveGuidance(id: String, version: Long, timescalesVersion: Option[Long] = None, ratesVersion: Option[Long] = None)(using request: Request[_]): Future[Result] = 
    adminService.getActive(id, version, timescalesVersion, ratesVersion).flatMap {
      case Right(process) => Future.successful(Ok(process))
      case Left(err) =>
        logger.error(s"Unable to retrieve archived process by id, err = $err")
        errorHandler.notFoundTemplate.map(BadRequest(_))
    }

}
