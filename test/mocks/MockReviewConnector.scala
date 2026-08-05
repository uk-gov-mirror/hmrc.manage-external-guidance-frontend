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

import connectors.ReviewConnector
import models.audit.AuditInfo
import models.{ApprovalProcessReview, ApprovalProcessStatusChange, PageReviewDetail, RequestOutcome}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockReviewConnector extends TestSuite with MockFactory {

  val mockReviewConnector: ReviewConnector = mock[ReviewConnector]

  object MockReviewConnector {

    def approval2iReview(id: String): CallHandler[Future[RequestOutcome[ApprovalProcessReview]]] =
      (mockReviewConnector
        .approval2iReview(_: String)(using _: ExecutionContext, _: HeaderCarrier))
        .expects(id, *, *)

    def approval2iReviewComplete(id: String, info: ApprovalProcessStatusChange): CallHandler[Future[RequestOutcome[AuditInfo]]] =
      (mockReviewConnector
        .approval2iReviewComplete(_: String, _: ApprovalProcessStatusChange)(using _: ExecutionContext, _: HeaderCarrier))
        .expects(id, info, *, *)

    def approval2iReviewPageInfo(id: String, pageUrl: String): CallHandler[Future[RequestOutcome[PageReviewDetail]]] =
      (mockReviewConnector
        .approval2iReviewPageInfo(_: String, _: String)(using _: ExecutionContext, _: HeaderCarrier))
        .expects(id, pageUrl, *, *)

    def approval2iReviewPageComplete(id: String, pageUrl: String, info: PageReviewDetail): CallHandler[Future[RequestOutcome[Unit]]] =
      (mockReviewConnector
        .approval2iReviewPageComplete(_: String, _: String, _: PageReviewDetail)(using _: ExecutionContext, _: HeaderCarrier))
        .expects(
          where { (i: String, u: String, p: PageReviewDetail, _: ExecutionContext, _: HeaderCarrier) =>
            i == id &&
            u == pageUrl &&
            p.id == info.id &&
            p.pageUrl == info.pageUrl &&
            p.result == info.result &&
            p.status == info.status &&
            p.updateUser == info.updateUser
          }
        )

    def approvalFactCheck(id: String): CallHandler[Future[RequestOutcome[ApprovalProcessReview]]] = {

      (mockReviewConnector
        .approvalFactCheck(_: String)(using _: ExecutionContext, _: HeaderCarrier))
        .expects(id, *, *)
    }

    def approvalFactCheckComplete(id: String, info: ApprovalProcessStatusChange): CallHandler[Future[RequestOutcome[AuditInfo]]] = {
      (mockReviewConnector
        .approvalFactCheckComplete(_: String, _: ApprovalProcessStatusChange)(using _: ExecutionContext, _: HeaderCarrier))
        .expects(id, info, *, *)
    }

    def factCheckPageInfo(id: String, pageUrl: String): CallHandler[Future[RequestOutcome[PageReviewDetail]]] =
      (mockReviewConnector
        .factCheckPageInfo(_: String, _: String)(using _: ExecutionContext, _: HeaderCarrier))
        .expects(id, pageUrl, *, *)

    def factCheckPageComplete(id: String, pageUrl: String, info: PageReviewDetail): CallHandler[Future[RequestOutcome[Unit]]] =
      (mockReviewConnector
        .factCheckPageComplete(_: String, _: String, _: PageReviewDetail)(using _: ExecutionContext, _: HeaderCarrier))
        .expects(
          where { (i: String, u: String, p: PageReviewDetail, _: ExecutionContext, _: HeaderCarrier) =>
            i == id &&
            u == pageUrl &&
            p.id == info.id &&
            p.pageUrl == info.pageUrl &&
            p.result == info.result &&
            p.status == info.status &&
            p.updateUser == info.updateUser
          }
        )

  }
}
