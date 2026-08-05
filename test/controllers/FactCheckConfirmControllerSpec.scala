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

import base.ControllerBaseSpec
import config.ErrorHandler
import controllers.actions.FakeFactCheckerAction
import mocks.{MockAuditService, MockReviewService}
import models.audit.{AuditInfo, FactCheckCompleteEvent}
import models.errors._
import models.{ApprovalStatus, ReviewData}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.{MimeTypes, Status}
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import views.html.{fact_check_complete, fact_check_confirm_error}

import scala.concurrent.Future

class FactCheckConfirmControllerSpec extends ControllerBaseSpec with GuiceOneAppPerSuite with MockReviewService with MockAuditService {

  private trait Test extends ReviewData {

    given hc: HeaderCarrier = HeaderCarrier()

    val errorHandler: ErrorHandler = injector.instanceOf[ErrorHandler]

    val view: fact_check_complete = injector.instanceOf[fact_check_complete]
    val errorView: fact_check_confirm_error = injector.instanceOf[fact_check_confirm_error]
    val auditInfo: AuditInfo = AuditInfo(credential, id, "title", 1, "author", 2, 2)
    val event: FactCheckCompleteEvent = FactCheckCompleteEvent(auditInfo)

    def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]
    given messages: Messages = messagesApi.preferred(FakeRequest("GET", "/"))

    val reviewController = new FactCheckConfirmController(
      errorHandler,
      FakeFactCheckerAction,
      view,
      errorView,
      mockReviewService,
      mockAuditService,
      messagesControllerComponents)

    val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
  }

  "The fact check controller when confirming the review" should {

    "display the correct view for a successful post of the review completion for a process" in new Test {

      MockAuditService.audit(event)

      MockReviewService
        .approvalFactCheckComplete(id, credential, name, ApprovalStatus.Complete)
        .returns(Future.successful(Right(auditInfo)))

      val result: Future[Result] = reviewController.onConfirm(id)(fakeRequest)

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some(MimeTypes.HTML)
      contentAsString(result) shouldBe view()(fakeRequest, messages).toString
    }
    "display the error view when the process is not in a state to be completed" in new Test {

      MockReviewService
        .approvalFactCheckComplete(id, credential, name, ApprovalStatus.Complete)
        .returns(Future.successful(Left(IncompleteDataError)))

      val result: Future[Result] = reviewController.onConfirm(id)(fakeRequest)

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some(MimeTypes.HTML)
      contentAsString(result) shouldBe errorView(id)(fakeRequest, messages).toString
    }

    "Return the Http status Not found when the process review does not exist" in new Test {

      MockReviewService
        .approvalFactCheckComplete(id, credential, name, ApprovalStatus.Complete)
        .returns(Future.successful(Left(NotFoundError)))

      val result: Future[Result] = reviewController.onConfirm(id)(fakeRequest)

      status(result) shouldBe Status.NOT_FOUND
    }

    "Return an Html error page when the process review does not exist" in new Test {

      MockReviewService
        .approvalFactCheckComplete(id, credential, name, ApprovalStatus.Complete)
        .returns(Future.successful(Left(NotFoundError)))

      val result: Future[Result] = reviewController.onConfirm(id)(fakeRequest)

      contentType(result) shouldBe Some(MimeTypes.HTML)
    }

    "Return the Http status internal server error when the process review data returned to the application is malformed" in new Test {

      MockReviewService
        .approvalFactCheckComplete(id, credential, name, ApprovalStatus.Complete)
        .returns(Future.successful(Left(MalformedResponseError)))

      val result: Future[Result] = reviewController.onConfirm(id)(fakeRequest)

      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "Return an Html error page when the process review data returned to the application is malformed" in new Test {

      MockReviewService
        .approvalFactCheckComplete(id, credential, name, ApprovalStatus.Complete)
        .returns(Future.successful(Left(MalformedResponseError)))

      val result: Future[Result] = reviewController.onConfirm(id)(fakeRequest)

      contentType(result) shouldBe Some(MimeTypes.HTML)
    }

    "Return the Http status not found when the process review data requested is out of date in some way" in new Test {

      MockReviewService
        .approvalFactCheckComplete(id, credential, name, ApprovalStatus.Complete)
        .returns(Future.successful(Left(StaleDataError)))

      val result: Future[Result] = reviewController.onConfirm(id)(fakeRequest)

      status(result) shouldBe Status.NOT_FOUND
    }

    "Return an Html error page when the process review data requested is out of date in some way" in new Test {

      MockReviewService
        .approvalFactCheckComplete(id, credential, name, ApprovalStatus.Complete)
        .returns(Future.successful(Left(StaleDataError)))

      val result: Future[Result] = reviewController.onConfirm(id)(fakeRequest)

      contentType(result) shouldBe Some(MimeTypes.HTML)
    }

    "Return the Http status internal server error when an unexpected error occurs" in new Test {

      MockReviewService
        .approvalFactCheckComplete(id, credential, name, ApprovalStatus.Complete)
        .returns(Future.successful(Left(InternalServerError)))

      val result: Future[Result] = reviewController.onConfirm(id)(fakeRequest)

      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "Return an Html error page when an unexpected error occurs" in new Test {

      MockReviewService
        .approvalFactCheckComplete(id, credential, name, ApprovalStatus.Complete)
        .returns(Future.successful(Left(InternalServerError)))

      val result: Future[Result] = reviewController.onConfirm(id)(fakeRequest)

      contentType(result) shouldBe Some(MimeTypes.HTML)
    }

  }

}
