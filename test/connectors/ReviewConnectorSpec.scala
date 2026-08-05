/*
 * Copyright 2024 HM Revenue & Customs
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

package connectors

import base.BaseSpec
import mocks.MockAppConfig
import models._
import models.audit.AuditInfo
import models.errors.{InternalServerError, MalformedResponseError, NotFoundError, StaleDataError}
import scala.concurrent.ExecutionContext.Implicits.global
import org.mockito.Mockito.when
import scala.concurrent.Future

class ReviewConnectorSpec extends BaseSpec {

  private trait Test extends ConnectorTest with ReviewData {

    val connector: ReviewConnector = new ReviewConnector(mockHttpClient, MockAppConfig)

    val reviewStatusChange: ApprovalProcessStatusChange = ApprovalProcessStatusChange("user", "email", ApprovalStatus.Published)
    val auditInfo: AuditInfo = AuditInfo("pid", "id", "title", 1, "author", 2, 2)
  }

  "Calling method approval2iReview with a valid id" should {

    "Return an instance of ApprovalProcessReview for a successful call" in new Test {

      when(requestBuilderExecute[RequestOutcome[ApprovalProcessReview]]).thenReturn(Future.successful(Right(reviewInfo)))

      val response: RequestOutcome[ApprovalProcessReview] = await(connector.approval2iReview(id))

      response shouldBe Right(reviewInfo)

    }

    "Return an instance of MalformedResponseError when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[ApprovalProcessReview]]).thenReturn(Future.successful(Left(MalformedResponseError)))

      val response = await(connector.approval2iReview(id))

      response shouldBe Left(MalformedResponseError)
    }

    "Return an instance of NotFoundError class when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[ApprovalProcessReview]]).thenReturn(Future.successful(Left(NotFoundError)))

      val response = await(connector.approval2iReview(id))

      response shouldBe Left(NotFoundError)
    }

    "Return an instance of StaleDataError class when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[ApprovalProcessReview]]).thenReturn(Future.successful(Left(StaleDataError)))

      val response = await(connector.approval2iReview(id))

      response shouldBe Left(StaleDataError)
    }

    "Return an instance of InternalServererror class when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[ApprovalProcessReview]]).thenReturn(Future.successful(Left(InternalServerError)))

      val response = await(connector.approval2iReview(id))

      response shouldBe Left(InternalServerError)
    }

  }

  "Calling method approval2iReviewComplete with a valid id and payload" should {

    "Return AuditInfo details for a successful call" in new Test {

      when(requestBuilderExecute[RequestOutcome[AuditInfo]]).thenReturn(Future.successful(Right(auditInfo)))

      val response = await(connector.approval2iReviewComplete(id, reviewStatusChange))

      response shouldBe Right(auditInfo)

    }

    "Return an instance of MalformedResponseError when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[AuditInfo]]).thenReturn(Future.successful(Left(MalformedResponseError)))

      val response = await(connector.approval2iReviewComplete(id, reviewStatusChange))

      response shouldBe Left(MalformedResponseError)
    }

    "Return an instance of NotFoundError class when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[AuditInfo]]).thenReturn(Future.successful(Left(NotFoundError)))

      val response = await(connector.approval2iReviewComplete(id, reviewStatusChange))

      response shouldBe Left(NotFoundError)
    }

    "Return an instance of StaleDataError class when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[AuditInfo]]).thenReturn(Future.successful(Left(StaleDataError)))

      val response = await(connector.approval2iReviewComplete(id, reviewStatusChange))

      response shouldBe Left(StaleDataError)
    }

    "Return an instance of InternalServererror class when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[AuditInfo]]).thenReturn(Future.successful(Left(InternalServerError)))

      val response = await(connector.approval2iReviewComplete(id, reviewStatusChange))

      response shouldBe Left(InternalServerError)
    }

  }

  "Calling method approvalFactCheck with a valid id" should {

    "Return an instance of ApprovalProcessReview for a successful call" in new Test {

      when(requestBuilderExecute[RequestOutcome[ApprovalProcessReview]]).thenReturn(Future.successful(Right(reviewInfo)))

      val response = await(connector.approvalFactCheck(id))

      response shouldBe Right(reviewInfo)

    }

    "Return an instance of MalformedResponseError when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[ApprovalProcessReview]]).thenReturn(Future.successful(Left(MalformedResponseError)))

      val response = await(connector.approvalFactCheck(id))

      response shouldBe Left(MalformedResponseError)
    }

    "Return an instance of NotFoundError class when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[ApprovalProcessReview]]).thenReturn(Future.successful(Left(NotFoundError)))

      val response = await(connector.approvalFactCheck(id))

      response shouldBe Left(NotFoundError)
    }

    "Return an instance of StaleDataError class when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[ApprovalProcessReview]]).thenReturn(Future.successful(Left(StaleDataError)))

      val response = await(connector.approvalFactCheck(id))

      response shouldBe Left(StaleDataError)
    }

    "Return an instance of InternalServererror class when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[ApprovalProcessReview]]).thenReturn(Future.successful(Left(InternalServerError)))

      val response = await(connector.approvalFactCheck(id))

      response shouldBe Left(InternalServerError)
    }

  }

  "Calling method approvalFactCheckComplete with a valid id and payload" should {

    "Return AuditInfo details for a successful call" in new Test {

      when(requestBuilderExecute[RequestOutcome[AuditInfo]]).thenReturn(Future.successful(Right(auditInfo)))

      val response = await(connector.approvalFactCheckComplete(id, reviewStatusChange))

      response shouldBe Right(auditInfo)

    }

    "Return an instance of MalformedResponseError when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[AuditInfo]]).thenReturn(Future.successful(Left(MalformedResponseError)))

      val response = await(connector.approvalFactCheckComplete(id, reviewStatusChange))

      response shouldBe Left(MalformedResponseError)
    }

    "Return an instance of NotFoundError class when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[AuditInfo]]).thenReturn(Future.successful(Left(NotFoundError)))

      val response = await(connector.approvalFactCheckComplete(id, reviewStatusChange))

      response shouldBe Left(NotFoundError)
    }

    "Return an instance of StaleDataError class when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[AuditInfo]]).thenReturn(Future.successful(Left(StaleDataError)))

      val response = await(connector.approvalFactCheckComplete(id, reviewStatusChange))

      response shouldBe Left(StaleDataError)
    }

    "Return an instance of InternalServererror class when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[AuditInfo]]).thenReturn(Future.successful(Left(InternalServerError)))

      val response = await(connector.approvalFactCheckComplete(id, reviewStatusChange))

      response shouldBe Left(InternalServerError)
    }

  }

  "Calling method approval2iReviewPageInfo with a valid id" should {

    "Return an instance of PageReviewDetail for a successful call" in new Test {

      when(requestBuilderExecute[RequestOutcome[PageReviewDetail]]).thenReturn(Future.successful(Right(reviewDetail)))

      val response = await(connector.approval2iReviewPageInfo(id, reviewDetail.pageUrl))

      response shouldBe Right(reviewDetail)

    }

    "Return an instance of MalformedResponseError when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[PageReviewDetail]]).thenReturn(Future.successful(Left(MalformedResponseError)))

      val response = await(connector.approval2iReviewPageInfo(id, reviewDetail.pageUrl))

      response shouldBe Left(MalformedResponseError)
    }

    "Return an instance of NotFoundError class when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[PageReviewDetail]]).thenReturn(Future.successful(Left(NotFoundError)))

      val response = await(connector.approval2iReviewPageInfo(id, reviewDetail.pageUrl))

      response shouldBe Left(NotFoundError)
    }

    "Return an instance of StaleDataError class when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[PageReviewDetail]]).thenReturn(Future.successful(Left(StaleDataError)))

      val response = await(connector.approval2iReviewPageInfo(id, reviewDetail.pageUrl))

      response shouldBe Left(StaleDataError)
    }

    "Return an instance of InternalServererror class when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[PageReviewDetail]]).thenReturn(Future.successful(Left(InternalServerError)))

      val response = await(connector.approval2iReviewPageInfo(id, reviewDetail.pageUrl))

      response shouldBe Left(InternalServerError)
    }

  }

  "Calling method approval2iReviewPageComplete with a valid id and payload" should {

    "Return true for a successful call" in new Test {

      when(requestBuilderExecute[RequestOutcome[Unit]]).thenReturn(Future.successful(Right(())))

      val response = await(connector.approval2iReviewPageComplete(id, updatedReviewDetail.pageUrl, updatedReviewDetail))

      response shouldBe Right(())

    }

    "Return an instance of MalformedResponseError when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[Unit]]).thenReturn(Future.successful(Left(MalformedResponseError)))

      val response = await(connector.approval2iReviewPageComplete(id, updatedReviewDetail.pageUrl, updatedReviewDetail))

      response shouldBe Left(MalformedResponseError)
    }

    "Return an instance of NotFoundError class when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[Unit]]).thenReturn(Future.successful(Left(NotFoundError)))

      val response = await(connector.approval2iReviewPageComplete(id, updatedReviewDetail.pageUrl, updatedReviewDetail))

      response shouldBe Left(NotFoundError)
    }

    "Return an instance of StaleDataError class when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[Unit]]).thenReturn(Future.successful(Left(StaleDataError)))

      val response = await(connector.approval2iReviewPageComplete(id, updatedReviewDetail.pageUrl, updatedReviewDetail))

      response shouldBe Left(StaleDataError)
    }

    "Return an instance of InternalServererror class when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[Unit]]).thenReturn(Future.successful(Left(InternalServerError)))

      val response = await(connector.approval2iReviewPageComplete(id, updatedReviewDetail.pageUrl, updatedReviewDetail))

      response shouldBe Left(InternalServerError)
    }

  }

  "Calling method factCheckPageInfo with a valid id" should {

    "Return an instance of PageReviewDetail for a successful call" in new Test {

      when(requestBuilderExecute[RequestOutcome[PageReviewDetail]]).thenReturn(Future.successful(Right(reviewDetail)))

      val response = await(connector.factCheckPageInfo(id, reviewDetail.pageUrl))

      response shouldBe Right(reviewDetail)

    }

    "Return an instance of MalformedResponseError when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[PageReviewDetail]]).thenReturn(Future.successful(Left(MalformedResponseError)))

      val response = await(connector.factCheckPageInfo(id, reviewDetail.pageUrl))

      response shouldBe Left(MalformedResponseError)
    }

    "Return an instance of NotFoundError class when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[PageReviewDetail]]).thenReturn(Future.successful(Left(NotFoundError)))

      val response = await(connector.factCheckPageInfo(id, reviewDetail.pageUrl))

      response shouldBe Left(NotFoundError)
    }

    "Return an instance of StaleDataError class when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[PageReviewDetail]]).thenReturn(Future.successful(Left(StaleDataError)))

      val response = await(connector.factCheckPageInfo(id, reviewDetail.pageUrl))

      response shouldBe Left(StaleDataError)
    }

    "Return an instance of InternalServererror class when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[PageReviewDetail]]).thenReturn(Future.successful(Left(InternalServerError)))

      val response = await(connector.factCheckPageInfo(id, reviewDetail.pageUrl))

      response shouldBe Left(InternalServerError)
    }

  }

  "Calling method factCheckPageComplete with a valid id and payload" should {

    "Return true for a successful call" in new Test {

      when(requestBuilderExecute[RequestOutcome[Unit]]).thenReturn(Future.successful(Right(())))

      val response = await(connector.factCheckPageComplete(id, updatedReviewDetail.pageUrl, updatedReviewDetail))

      response shouldBe Right(())

    }

    "Return an instance of MalformedResponseError when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[Unit]]).thenReturn(Future.successful(Left(MalformedResponseError)))

      val response = await(connector.factCheckPageComplete(id, updatedReviewDetail.pageUrl, updatedReviewDetail))

      response shouldBe Left(MalformedResponseError)
    }

    "Return an instance of NotFoundError class when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[Unit]]).thenReturn(Future.successful(Left(NotFoundError)))

      val response = await(connector.factCheckPageComplete(id, updatedReviewDetail.pageUrl, updatedReviewDetail))

      response shouldBe Left(NotFoundError)
    }

    "Return an instance of StaleDataError class when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[Unit]]).thenReturn(Future.successful(Left(StaleDataError)))

      val response = await(connector.factCheckPageComplete(id, updatedReviewDetail.pageUrl, updatedReviewDetail))

      response shouldBe Left(StaleDataError)
    }

    "Return an instance of InternalServererror class when an error occurs" in new Test {

      when(requestBuilderExecute[RequestOutcome[Unit]]).thenReturn(Future.successful(Left(InternalServerError)))

      val response = await(connector.factCheckPageComplete(id, updatedReviewDetail.pageUrl, updatedReviewDetail))

      response shouldBe Left(InternalServerError)
    }

  }

}
