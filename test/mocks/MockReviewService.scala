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

import models.audit.AuditInfo
import models.{ApprovalProcessReview, ApprovalStatus, PageReviewDetail, RequestOutcome}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import services.ReviewService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockReviewService extends TestSuite with MockFactory {

  val mockReviewService: ReviewService = mock[ReviewService]

  object MockReviewService {

    def approval2iReview(id: String): CallHandler[Future[RequestOutcome[ApprovalProcessReview]]] =
      (mockReviewService
        .approval2iReview(_: String)(using _: ExecutionContext, _: HeaderCarrier))
        .expects(id, *, *)

    def approval2iReviewCheck(id: String): CallHandler[Future[RequestOutcome[Unit]]] =
      (mockReviewService
        .approval2iReviewCheck(_: String)(using _: ExecutionContext, _: HeaderCarrier))
        .expects(id, *, *)

    def approval2iReviewComplete(id: String,
                                 userPid: String,
                                 userName: String,
                                 status: ApprovalStatus): CallHandler[Future[RequestOutcome[AuditInfo]]] =
      (mockReviewService
        .approval2iReviewComplete(_: String, _: String, _: String, _: ApprovalStatus)(using _: ExecutionContext, _: HeaderCarrier))
        .expects(id, userPid, userName, status, *, *)

    def approval2iPageReview(id: String, pageUrl: String): CallHandler[Future[RequestOutcome[PageReviewDetail]]] =
      (mockReviewService
        .approval2iPageReview(_: String, _: String)(using _: ExecutionContext, _: HeaderCarrier))
        .expects(id, pageUrl, *, *)

    def approval2iPageReviewComplete(id: String, pageUrl: String, pageReviewDetail: PageReviewDetail): CallHandler[Future[RequestOutcome[Unit]]] =
      (mockReviewService
        .approval2iPageReviewComplete(_: String, _: String, _: PageReviewDetail)(using _: ExecutionContext, _: HeaderCarrier))
        .expects(
          where { (i: String, u: String, p: PageReviewDetail, _: ExecutionContext, _: HeaderCarrier) =>
            i == id &&
            u == pageUrl &&
            p.id == pageReviewDetail.id &&
            p.pageUrl == pageReviewDetail.pageUrl &&
            p.result == pageReviewDetail.result &&
            p.status == pageReviewDetail.status &&
            p.updateUser == pageReviewDetail.updateUser
          }
        )

    def approvalFactCheck(id: String): CallHandler[Future[RequestOutcome[ApprovalProcessReview]]] =
      (mockReviewService
        .approvalFactCheck(_: String)(using _: ExecutionContext, _: HeaderCarrier))
        .expects(id, *, *)

    def approvalFactCheckComplete(id: String,
                                  userId: String,
                                  userName: String,
                                  status: ApprovalStatus): CallHandler[Future[RequestOutcome[AuditInfo]]] =
      (mockReviewService
        .approvalFactCheckComplete(_: String, _: String, _:String, _: ApprovalStatus)(using _: ExecutionContext, _: HeaderCarrier))
        .expects(id, userId, userName, status, *, *)

    def factCheckPageInfo(id: String, pageUrl: String): CallHandler[Future[RequestOutcome[PageReviewDetail]]] =
      (mockReviewService
        .factCheckPageInfo(_: String, _: String)(using _: ExecutionContext, _: HeaderCarrier))
        .expects(id, pageUrl, *, *)

    def factCheckPageComplete(id: String, pageUrl: String, pageReviewDetail: PageReviewDetail): CallHandler[Future[RequestOutcome[Unit]]] =
      (mockReviewService
        .factCheckPageComplete(_: String, _: String, _: PageReviewDetail)(using _: ExecutionContext, _: HeaderCarrier))
        .expects(
          where { (i: String, u: String, p: PageReviewDetail, _: ExecutionContext, _: HeaderCarrier) =>
            i == id &&
            u == pageUrl &&
            p.id == pageReviewDetail.id &&
            p.pageUrl == pageReviewDetail.pageUrl &&
            p.result == pageReviewDetail.result &&
            p.status == pageReviewDetail.status &&
            p.updateUser == pageReviewDetail.updateUser
          }
        )
  }

}
