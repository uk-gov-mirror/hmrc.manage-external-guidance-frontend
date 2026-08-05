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

import config.{AppConfig, ErrorHandler}
import controllers.actions.AuthorisedAction
import forms.AdminSignInForm
import models.AdminSignInDetails
import play.api.mvc.*
import services.ProcessAdminService
import views.html.process_admin.{admin_signin, approval_summaries, archived_summaries, published_summaries, active_summaries}
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import models.admin.navigation.{AdminPage, PublishedList, ApprovalList, ArchivedList, ActiveList, Timescales, Rates}

object ProcessAdminController {
  val userSessionKey = "userName"
}

@Singleton
class ProcessAdminController @Inject() (
    appConfig: AppConfig,
    authAction: AuthorisedAction,
    errorHandler: ErrorHandler,
    publishedView: published_summaries,
    archivedView: archived_summaries,
    approvalsView: approval_summaries,
    activeView: active_summaries,
    signin: admin_signin,
    adminService: ProcessAdminService,
    mcc: MessagesControllerComponents
) extends AbstractProcessAdminController(errorHandler, publishedView, archivedView, approvalsView, activeView, adminService, mcc) {

  val pages: List[AdminPage] = List(
    AdminPage(PublishedList, s"/external-guidance${controllers.routes.ProcessAdminController.listPublished.url}", "published.switch"),
    AdminPage(ApprovalList, s"/external-guidance${controllers.routes.ProcessAdminController.listApprovals.url}", "approvals.switch"),
    AdminPage(ArchivedList, s"/external-guidance${controllers.routes.ProcessAdminController.listArchived.url}", "archived.switch"),
    AdminPage(ActiveList, s"/external-guidance${controllers.routes.ProcessAdminController.listActive.url}", "active.switch"),
    AdminPage(Timescales, s"/external-guidance${controllers.routes.TimescalesController.getData.url}", "timescales.download.button", true),
    AdminPage(Rates, s"/external-guidance${controllers.routes.RatesController.getData.url}", "rates.download.button", true)
  )

  def signIn: Action[AnyContent] = Action { request =>
    given Request[AnyContent] = request
    Ok(signin(AdminSignInForm.form))
  }

  def submitSignIn: Action[AnyContent] = Action.async { request =>
    given Request[AnyContent] = request
    AdminSignInForm.form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(signin(formWithErrors))),
        form =>
          AdminSignInDetails(form.name, form.password)
            .validate(appConfig)
            .fold({
              val formWithError = AdminSignInForm.form
                                    .fill(AdminSignInDetails("",""))
                                    .withGlobalError(request.messages("admin.signin-error"))
              Future.successful(Unauthorized(signin(formWithError)(request, request.messages)))
            }){ user =>
              Future.successful(Redirect(routes.ProcessAdminController.listPublished.url).addingToSession(ProcessAdminController.userSessionKey -> user))
            }
      )
  }

  def signOut: Action[AnyContent] = authAction.async { request =>
    given Request[AnyContent] = request  
    Future.successful(Redirect(routes.ProcessAdminController.listPublished.url).removingFromSession(ProcessAdminController.userSessionKey))
  }

  def listPublished: Action[AnyContent] = authAction.async { request =>
    given Request[AnyContent] = request
    published(routes.ProcessAdminController.getPublished _)
  }

  def getPublished(processCode: String): Action[AnyContent] = authAction.async { request =>
    given Request[AnyContent] = request
    getPublishedGuidance(processCode)
  }

  def listApprovals: Action[AnyContent] = authAction.async { request =>
    given Request[AnyContent] = request
    approvals(routes.ProcessAdminController.getApproval _)
  }

  def getApproval(processCode: String): Action[AnyContent] = authAction.async { request =>
    given Request[AnyContent] = request
    getApprovalGuidance(processCode)
  }

  def listArchived: Action[AnyContent] = authAction.async { request =>
    given Request[AnyContent] = request
    archived(routes.ProcessAdminController.getArchived _)
  }

  def getArchived(id: String): Action[AnyContent] = authAction.async { request =>
    given Request[AnyContent] = request
    getArchivedGuidance(id)
  }

  def listActive: Action[AnyContent] = authAction.async { request =>
    given Request[AnyContent] = request
    active(routes.ProcessAdminController.getActive _)
  }

  def getActive(id: String, version: Long, timescalesVersion: Option[Long], ratesVersion: Option[Long]): Action[AnyContent] = authAction.async { request =>
    given Request[AnyContent] = request  
    getActiveGuidance(id, version, timescalesVersion, ratesVersion) 
  }
}
