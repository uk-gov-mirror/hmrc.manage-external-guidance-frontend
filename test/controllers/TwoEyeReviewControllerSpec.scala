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
import controllers.actions.FakeTwoEyeReviewerAction
import mocks.{MockAuditService, MockReviewService}
import java.time.LocalDate
import models.errors._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import models.{ApprovalProcessSummary, ApprovalStatus, ReviewData}
import models.ReviewType.ReviewType2i
import play.api.http.{MimeTypes, Status}
import play.api.i18n.{Messages, MessagesApi}
import models.ApprovalStatus.Published
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import views.html.{duplicate_process_code_error, twoeye_content_review}
import views.html.{twoeye_published, twoeye_confirm_error}
import models.audit.{AuditInfo, TwoEyeReviewCompleteEvent, PublishedEvent}
import scala.concurrent.Future

class TwoEyeReviewControllerSpec extends ControllerBaseSpec with GuiceOneAppPerSuite with MockReviewService with MockAuditService {

  private trait Test extends ReviewData {

    given hc: HeaderCarrier = HeaderCarrier()

    val errorHandler: ErrorHandler = injector.instanceOf[ErrorHandler]
    val view = injector.instanceOf[twoeye_content_review]
    val duplicateView = injector.instanceOf[duplicate_process_code_error]
    val confirmationView: twoeye_published = injector.instanceOf[twoeye_published]
    val errorView: twoeye_confirm_error = injector.instanceOf[twoeye_confirm_error]
    val approvalProcessSummary: ApprovalProcessSummary = ApprovalProcessSummary("id", "title", LocalDate.now, ApprovalStatus.Published, ReviewType2i, 1)
    val auditInfo: AuditInfo = AuditInfo(credential, id, "title", 1, "author", 2, 2)
    val event: TwoEyeReviewCompleteEvent = TwoEyeReviewCompleteEvent(auditInfo)
    val publishedEvent: PublishedEvent = PublishedEvent(auditInfo)
    def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]


    val reviewController =
      new TwoEyeReviewController(
        errorHandler,
        FakeTwoEyeReviewerAction,
        view,
        duplicateView,
        confirmationView,
        errorView,
        mockAuditService,
        mockReviewService,
        messagesControllerComponents)


    given messages: Messages = messagesApi.preferred(FakeRequest("GET", "/"))
    val fakeGetRequest = FakeRequest("GET", "/")
    val fakePostRequest: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest("POST", "/").withFormUrlEncodedBody()
  }

  "GET two eye review controller" should {

    "Return 200 for a successful retrieval of the review for a process" in new Test {

      MockReviewService.approval2iReview(id).returns(Future.successful(Right(reviewInfo)))

      val result: Future[Result] = reviewController.approval(id)(fakeGetRequest)

      status(result) shouldBe Status.OK
    }

    "Return an Html document displaying the details of the review" in new Test {

      MockReviewService.approval2iReview(id).returns(Future.successful(Right(reviewInfo)))

      val result: Future[Result] = reviewController.approval(id)(fakeGetRequest)

      contentType(result) shouldBe Some(MimeTypes.HTML)
    }

    "Return the Http status Not found when the process review does not exist" in new Test {

      MockReviewService.approval2iReview(id).returns(Future.successful(Left(NotFoundError)))

      val result: Future[Result] = reviewController.approval(id)(fakeGetRequest)

      status(result) shouldBe Status.NOT_FOUND
    }

    "Return an Html error page when the process review does not exist" in new Test {

      MockReviewService.approval2iReview(id).returns(Future.successful(Left(NotFoundError)))

      val result: Future[Result] = reviewController.approval(id)(fakeGetRequest)

      contentType(result) shouldBe Some(MimeTypes.HTML)
    }

    "Return the Http status internal server error when the process review data returned to the application is malformed" in new Test {

      MockReviewService.approval2iReview(id).returns(Future.successful(Left(MalformedResponseError)))

      val result: Future[Result] = reviewController.approval(id)(fakeGetRequest)

      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "Return an Html error page when the process review data returned to the application is malformed" in new Test {

      MockReviewService.approval2iReview(id).returns(Future.successful(Left(MalformedResponseError)))

      val result: Future[Result] = reviewController.approval(id)(fakeGetRequest)

      contentType(result) shouldBe Some(MimeTypes.HTML)
    }

    "Return the Http status not found when the process review data requested is out of date in some way" in new Test {

      MockReviewService.approval2iReview(id).returns(Future.successful(Left(StaleDataError)))

      val result: Future[Result] = reviewController.approval(id)(fakeGetRequest)

      status(result) shouldBe Status.NOT_FOUND
    }

    "Return an Html error page when the process review data requested is out of date in some way" in new Test {

      MockReviewService.approval2iReview(id).returns(Future.successful(Left(StaleDataError)))

      val result: Future[Result] = reviewController.approval(id)(fakeGetRequest)

      contentType(result) shouldBe Some(MimeTypes.HTML)
    }

    "Return the Http status internal server error when an unexpected error occurs" in new Test {

      MockReviewService.approval2iReview(id).returns(Future.successful(Left(InternalServerError)))

      val result: Future[Result] = reviewController.approval(id)(fakeGetRequest)

      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "Return an Html error page when an unexpected error occurs" in new Test {

      MockReviewService.approval2iReview(id).returns(Future.successful(Left(InternalServerError)))

      val result: Future[Result] = reviewController.approval(id)(fakeGetRequest)

      contentType(result) shouldBe Some(MimeTypes.HTML)
    }

    "Return the Http status BadRequest and display a view when the system identifies a duplicate" in new Test {

      MockReviewService.approval2iReview(id).returns(Future.successful(Left(DuplicateKeyError)))

      val result: Future[Result] = reviewController.approval(id)(fakeGetRequest)

      status(result) shouldBe Status.BAD_REQUEST
      contentType(result) shouldBe Some(MimeTypes.HTML)
    }


  }

  "POST The two eye review controller" should {

    "Return the confirmation view for a successful post of the review completion for a process" in new Test {

      MockReviewService
        .approval2iReviewCheck(id)
        .returns(Future.successful(Right(())))
      MockReviewService
        .approval2iReviewComplete(id, credential, name, Published)
        .returns(Future.successful(Right(auditInfo)))
      MockAuditService.audit(event).returns(Future.successful(()))
      MockAuditService.audit(publishedEvent).returns(Future.successful(()))

      val result: Future[Result] = reviewController.onSubmit(id)(fakePostRequest)

      contentType(result) shouldBe Some("text/html")
      contentAsString(result) shouldBe confirmationView()(using fakeGetRequest, messages).toString
    }

    "Return the Http status Not found when the process review does not exist" in new Test {

      MockReviewService
        .approval2iReviewCheck(id)
        .returns(Future.successful(Right(())))
      MockReviewService
        .approval2iReviewComplete(id, credential, name, Published)
        .returns(Future.successful(Left(NotFoundError)))

      val result: Future[Result] = reviewController.onSubmit(id)(fakePostRequest)

      status(result) shouldBe Status.NOT_FOUND
    }

    "Return an Html error page when the process review does not exist" in new Test {

      MockReviewService
        .approval2iReviewCheck(id)
        .returns(Future.successful(Right(())))
      MockReviewService
        .approval2iReviewComplete(id, credential, name, Published)
        .returns(Future.successful(Left(NotFoundError)))

      val result: Future[Result] = reviewController.onSubmit(id)(fakePostRequest)

      contentType(result) shouldBe Some(MimeTypes.HTML)
    }

    "Return the Http status internal server error when the process review data returned to the application is malformed" in new Test {

      MockReviewService
        .approval2iReviewCheck(id)
        .returns(Future.successful(Right(())))
      MockReviewService
        .approval2iReviewComplete(id, credential, name, Published)
        .returns(Future.successful(Left(MalformedResponseError)))

      val result: Future[Result] = reviewController.onSubmit(id)(fakePostRequest)

      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "Return an Html error page when the process review data returned to the application is malformed" in new Test {

      MockReviewService
        .approval2iReviewCheck(id)
        .returns(Future.successful(Right(())))
      MockReviewService
        .approval2iReviewComplete(id, credential, name, Published)
        .returns(Future.successful(Left(MalformedResponseError)))

      val result: Future[Result] = reviewController.onSubmit(id)(fakePostRequest)

      contentType(result) shouldBe Some(MimeTypes.HTML)
    }

    "Return the Http status not found when the process review data requested is out of date in some way" in new Test {

      MockReviewService
        .approval2iReviewCheck(id)
        .returns(Future.successful(Right(())))
      MockReviewService
        .approval2iReviewComplete(id, credential, name, Published)
        .returns(Future.successful(Left(StaleDataError)))

      val result: Future[Result] = reviewController.onSubmit(id)(fakePostRequest)

      status(result) shouldBe Status.NOT_FOUND
    }

    "Return the Http status not found when the specified process cannot be found" in new Test {

      MockReviewService
        .approval2iReviewCheck(id)
        .returns(Future.successful(Left(NotFoundError)))

      val result: Future[Result] = reviewController.onSubmit(id)(fakePostRequest)

      status(result) shouldBe Status.NOT_FOUND
    }

    "Return the Http status not found when stale data error returned from backend" in new Test {

      MockReviewService
        .approval2iReviewCheck(id)
        .returns(Future.successful(Left(StaleDataError)))

      val result: Future[Result] = reviewController.onSubmit(id)(fakePostRequest)

      status(result) shouldBe Status.NOT_FOUND
    }

    "Return the Http status internal server error when an unexpected error occurs within approval2iReviewCheck" in new Test {

      MockReviewService
        .approval2iReviewCheck(id)
        .returns(Future.successful(Left(InternalServerError)))

      val result: Future[Result] = reviewController.onSubmit(id)(fakePostRequest)

      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "Return an Html error page when the process review data requested is out of date in some way" in new Test {

      MockReviewService
        .approval2iReviewCheck(id)
        .returns(Future.successful(Right(())))
      MockReviewService
        .approval2iReviewComplete(id, credential, name, Published)
        .returns(Future.successful(Left(StaleDataError)))

      val result: Future[Result] = reviewController.onSubmit(id)(fakePostRequest)

      contentType(result) shouldBe Some(MimeTypes.HTML)
    }

    "Return the Http status internal server error when an unexpected error occurs" in new Test {

      MockReviewService
        .approval2iReviewCheck(id)
        .returns(Future.successful(Right(())))
      MockReviewService
        .approval2iReviewComplete(id, credential, name, Published)
        .returns(Future.successful(Left(InternalServerError)))

      val result: Future[Result] = reviewController.onSubmit(id)(fakePostRequest)

      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "Return an Html error page when an unexpected error occurs" in new Test {

      MockReviewService
        .approval2iReviewCheck(id)
        .returns(Future.successful(Right(())))
      MockReviewService
        .approval2iReviewComplete(id, credential, name, Published)
        .returns(Future.successful(Left(InternalServerError)))

      val result: Future[Result] = reviewController.onSubmit(id)(fakePostRequest)

      contentType(result) shouldBe Some(MimeTypes.HTML)
    }

    "Return a bad request and display an error page when duplicate key detected" in new Test {

      MockReviewService
        .approval2iReviewCheck(id)
        .returns(Future.successful(Right(())))
      MockReviewService
        .approval2iReviewComplete(id, credential, name, Published)
        .returns(Future.successful(Left(DuplicateKeyError)))

      val result: Future[Result] = reviewController.onSubmit(id)(fakePostRequest)

      status(result) shouldBe Status.BAD_REQUEST
      contentType(result) shouldBe Some(MimeTypes.HTML)
    }

  }


}
