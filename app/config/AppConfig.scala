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

package config

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

trait AppConfig {
  lazy val appName: String
  lazy val reportAProblemPartialUrl: String
  lazy val reportAProblemNonJSUrl: String
  lazy val externalGuidanceBaseUrl: String
  lazy val loginUrl: String
  lazy val continueUrl: String
  lazy val timescalesContinueUrl: String
  lazy val ratesContinueUrl: String
  lazy val processAdminContinueUrl: String
  lazy val designerAdminContinueUrl: String
  lazy val designerRole: String
  lazy val twoEyeReviewerRole: String
  lazy val factCheckerRole: String
  lazy val viewApprovalUrl: String
  lazy val pageMapApprovalUrl: String
  lazy val pageMapPublishedlUrl: String
  lazy val activeProcessesUrl: String
  lazy val commentsAndFeedbackUrl: String
  lazy val processAdminUser: String
  lazy val processAdminPassword: String
  lazy val debugApprovalUrl: String
  lazy val debugPublishedUrl: String
}

@Singleton
class AppConfigImpl @Inject() (config: Configuration, servicesConfig: ServicesConfig) extends AppConfig {

  private lazy val host = servicesConfig.getString("host")
  private lazy val contactBaseUrl = servicesConfig.baseUrl("contact-frontend")
  private lazy val serviceIdentifier = servicesConfig.getString("contact-frontend-urls.serviceIdentifier")
  private lazy val egViewerHost: String = config.get[String]("external-guidance-viewer.host")
  private lazy val egViewerApiHost: String = config.get[String]("external-guidance-viewer.api-host")
  private lazy val egAdminBaseUrl: String = config.get[String]("external-guidance-viewer.adminBaseUrl")

  lazy val reportAProblemPartialUrl: String = s"$contactBaseUrl/contact/problem_reports_ajax?service=$serviceIdentifier"
  lazy val reportAProblemNonJSUrl: String = s"""$contactBaseUrl${servicesConfig.getString("contact-frontend-urls.reportAProblemNonJSUrl")}"""
  lazy val externalGuidanceBaseUrl: String = servicesConfig.baseUrl("external-guidance")
  lazy val appName: String = config.get[String]("appName")
  lazy val commentsAndFeedbackUrl: String = config.get[String]("appLinks.commentsAndFeedbackUrl")
  lazy val loginUrl: String = servicesConfig.getString("strideAuth.login.url")
  lazy val continueUrl: String = host + servicesConfig.getString("strideAuth.login.continueUrl")
  lazy val timescalesContinueUrl: String = host + servicesConfig.getString("strideAuth.login.timescalesContinueUrl")
  lazy val ratesContinueUrl: String = host + servicesConfig.getString("strideAuth.login.ratesContinueUrl")
  lazy val processAdminContinueUrl: String = host + servicesConfig.getString("strideAuth.login.processAdminContinueUrl")
  lazy val designerAdminContinueUrl: String = host + servicesConfig.getString("strideAuth.login.designerAdminContinueUrl")
  lazy val designerRole: String = servicesConfig.getString("strideAuth.roles.designer")
  lazy val twoEyeReviewerRole: String = servicesConfig.getString("strideAuth.roles.twoEyeReviewer")
  lazy val factCheckerRole: String = servicesConfig.getString("strideAuth.roles.factChecker")
  lazy val debugApprovalUrl: String = s"$egViewerHost$egAdminBaseUrl/debug${config.get[String]("external-guidance-viewer.approvalUrl")}" 
  lazy val debugPublishedUrl: String = s"$egViewerHost$egAdminBaseUrl/debug${config.get[String]("external-guidance-viewer.publishedUrl")}" 
  lazy val viewApprovalUrl: String = s"$egViewerHost$egAdminBaseUrl${config.get[String]("external-guidance-viewer.approvalUrl")}"
  lazy val pageMapApprovalUrl: String = s"$egViewerHost$egAdminBaseUrl${config.get[String]("external-guidance-viewer.pageMapApprovalUrl")}"
  lazy val pageMapPublishedlUrl: String = s"$egViewerHost$egAdminBaseUrl${config.get[String]("external-guidance-viewer.pageMapPublishedUrl")}"
  lazy val activeProcessesUrl: String = s"$egViewerApiHost$egAdminBaseUrl${config.get[String]("external-guidance-viewer.activeProcessesUrl")}"
  lazy val processAdminUser: String = config.get[String]("admin-username")
  lazy val processAdminPassword: String = config.get[String]("admin-password")
}
