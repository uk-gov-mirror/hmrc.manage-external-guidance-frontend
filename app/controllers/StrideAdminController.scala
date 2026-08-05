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
import controllers.actions.DesignerAction
import play.api.mvc.*
import services.ProcessAdminService
import views.html.process_admin.{approval_summaries, archived_summaries, published_summaries, active_summaries}
import javax.inject.{Inject, Singleton}
import models.admin.navigation.{AdminPage, PublishedList, ApprovalList}

@Singleton
class StrideAdminController @Inject() (
    designerAuthenticatedAction: DesignerAction,
    errorHandler: ErrorHandler,
    publishedView: published_summaries,
    archivedView: archived_summaries,
    approvalsView: approval_summaries,
    activeView: active_summaries,
    adminService: ProcessAdminService,
    mcc: MessagesControllerComponents
) extends AbstractProcessAdminController(errorHandler, publishedView, archivedView, approvalsView, activeView, adminService, mcc) {

  val pages: List[AdminPage] = List(
    AdminPage(PublishedList, s"/external-guidance${controllers.routes.StrideAdminController.listPublished.url}", "published.switch"),
    AdminPage(ApprovalList, s"/external-guidance${controllers.routes.StrideAdminController.listApprovals.url}", "approvals.switch")
  )

  def listPublished: Action[AnyContent] = designerAuthenticatedAction.async { request =>
    given Request[AnyContent] = request
    published(routes.StrideAdminController.getPublished _)
  }

  def getPublished(processCode: String): Action[AnyContent] = designerAuthenticatedAction.async { request =>
    given Request[AnyContent] = request
    getPublishedGuidance(processCode)
  }

  def listApprovals: Action[AnyContent] = designerAuthenticatedAction.async { request =>
    given Request[AnyContent] = request
    approvals(routes.StrideAdminController.getApproval _)
  }

  def getApproval(processCode: String): Action[AnyContent] = designerAuthenticatedAction.async { request =>
    given Request[AnyContent] = request
    getApprovalGuidance(processCode)
  }

}
