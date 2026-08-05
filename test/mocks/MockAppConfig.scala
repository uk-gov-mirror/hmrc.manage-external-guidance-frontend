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

package mocks

import config.AppConfig

object MockAppConfig extends AppConfig {
  override lazy val reportAProblemPartialUrl: String = "someUrl"
  override lazy val reportAProblemNonJSUrl: String = "someJsUrl"
  override lazy val externalGuidanceBaseUrl: String = "http://external-guidance-base-url"
  override lazy val appName: String = "manage-external-guidance-frontend"
  lazy val loginUrl: String = "http://localhost:9041/stride/sign-in"
  lazy val continueUrl: String = "http://localhost:9740/external-guidance/hello-world"
  lazy val designerRole: String = "Designer"
  lazy val twoEyeReviewerRole: String = "2iReviewer"
  lazy val factCheckerRole: String = "FactChecker"
  lazy val viewApprovalUrl: String = "http://localhost:9741/review-guidance/approval"
  lazy val pageMapApprovalUrl: String = "http://localhost:9741/review-guidance/map-approval"
  lazy val pageMapPublishedlUrl: String = "http://localhost:9741/review-guidance/map-published"
  lazy val activeProcessesUrl: String = "http://localhost:9741/review-guidance/active"
  lazy val commentsAndFeedbackUrl: String = "http://www.gov.uk"
  lazy val timescalesContinueUrl: String = "http://localhost:9740/external-guidance/timescales"
  lazy val ratesContinueUrl: String = "http://localhost:9740/external-guidance/rates"
  lazy val processAdminContinueUrl: String = "http://localhost:9740/external-guidance/admin"
  lazy val designerAdminContinueUrl: String = "http://localhost:9740/external-guidance/designer"
  lazy val processAdminUser: String = "admin"
  lazy val processAdminPassword: String = "password"
  lazy val debugApprovalUrl: String = "http://localhost:9741/review-guidance/debug/approval"
  lazy val debugPublishedUrl: String ="http://localhost:9741/review-guidance/debug/published"
}
