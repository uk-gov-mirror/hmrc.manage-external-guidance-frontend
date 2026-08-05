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

package services

import connectors.ReviewConnector
import javax.inject.{Inject, Singleton}
import models.*
import models.audit.AuditInfo
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReviewService @Inject() (reviewConnector: ReviewConnector) {

  def approval2iReview(id: String)(using ex: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[ApprovalProcessReview]] =
    reviewConnector.approval2iReview(id)

  def approval2iReviewCheck(id: String)(using ex: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[Unit]] =
    reviewConnector.approval2iReviewConfirmCheck(id)

  def approval2iReviewComplete(id: String, userPid: String, userName: String, status: ApprovalStatus)(
      using ex: ExecutionContext,
      hc: HeaderCarrier
  ): Future[RequestOutcome[AuditInfo]] =
    reviewConnector.approval2iReviewComplete(id, ApprovalProcessStatusChange(userPid, userName, status))

  def approval2iPageReview(id: String, pageUrl: String)(using ex: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[PageReviewDetail]] =
    reviewConnector.approval2iReviewPageInfo(id, pageUrl)

  def approval2iPageReviewComplete(id: String, pageUrl: String, pageReviewDetail: PageReviewDetail)(
      using ex: ExecutionContext,
      hc: HeaderCarrier
  ): Future[RequestOutcome[Unit]] =
    reviewConnector.approval2iReviewPageComplete(id, pageUrl, pageReviewDetail)

  def approvalFactCheck(id: String)(
      using ex: ExecutionContext,
      hc: HeaderCarrier
  ): Future[RequestOutcome[ApprovalProcessReview]] =
    reviewConnector.approvalFactCheck(id)

  def approvalFactCheckComplete(id: String, userPid: String, userName: String, status: ApprovalStatus)(
      using ex: ExecutionContext,
      hc: HeaderCarrier
  ): Future[RequestOutcome[AuditInfo]] =
    reviewConnector.approvalFactCheckComplete(id, ApprovalProcessStatusChange(userPid, userName, status))

  def factCheckPageInfo(id: String, pageUrl: String)(using ex: ExecutionContext, hc: HeaderCarrier): Future[RequestOutcome[PageReviewDetail]] =
    reviewConnector.factCheckPageInfo(id, pageUrl)

  def factCheckPageComplete(id: String, pageUrl: String, pageReviewDetail: PageReviewDetail)(
      using ex: ExecutionContext,
      hc: HeaderCarrier
  ): Future[RequestOutcome[Unit]] =
    reviewConnector.factCheckPageComplete(id, pageUrl, pageReviewDetail)

}
