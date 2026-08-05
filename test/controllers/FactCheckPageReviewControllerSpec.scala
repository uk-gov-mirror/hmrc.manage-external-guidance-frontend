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
import forms.FactCheckPageReviewFormProvider
import mocks.MockReviewService
import models.ReviewData
import models.errors.{InternalServerError, MalformedResponseError, NotFoundError, StaleDataError}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.{MimeTypes, Status}
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import views.html.fact_check_page_review

import scala.concurrent.Future

class FactCheckPageReviewControllerSpec extends ControllerBaseSpec with GuiceOneAppPerSuite with MockReviewService {

  private trait Test extends ReviewData {

    given hc: HeaderCarrier = HeaderCarrier()

    val errorHandler: ErrorHandler = injector.instanceOf[ErrorHandler]
    val view: fact_check_page_review = injector.instanceOf[fact_check_page_review]
    val formProvider = new FactCheckPageReviewFormProvider()

    val reviewController =
      new FactCheckPageReviewController(errorHandler, FakeFactCheckerAction, formProvider, view, mockReviewService, messagesControllerComponents)

    val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("POST", "/")
  }
  "The fact check controller when loading the page" should {
    "Return OK when the page data is retrieved successfully" in new Test {

      MockReviewService
        .factCheckPageInfo(id, reviewDetail.pageUrl)
        .returns(Future.successful(Right(reviewDetail)))

      val result: Future[Result] = reviewController.onPageLoad(id, updatedReviewDetail.pageUrl.drop(1), 1)(fakeRequest)

      status(result) shouldBe Status.OK
    }

    "Return OK when the page data is retrieved successfully and contains a result" in new Test {

      MockReviewService
        .factCheckPageInfo(id, reviewDetail.pageUrl)
        .returns(Future.successful(Right(updatedReviewDetail)))

      val result: Future[Result] = reviewController.onPageLoad(id, updatedReviewDetail.pageUrl.drop(1), 1)(fakeRequest)

      status(result) shouldBe Status.OK
    }

    "Return InternalServerError when the page data fails to retrieve" in new Test {

      MockReviewService
        .factCheckPageInfo(id, reviewDetail.pageUrl)
        .returns(Future.successful(Left(StaleDataError)))

      val result: Future[Result] = reviewController.onPageLoad(id, updatedReviewDetail.pageUrl.drop(1), 1)(fakeRequest)

      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }
  }

  "The fact check controller when submitting the page" should {

    "Return SEE_OTHER for a successful post of the review completion for a process" in new Test {

      MockReviewService
        .factCheckPageComplete(id, updatedReviewDetail.pageUrl, updatedReviewDetail)
        .returns(Future.successful(Right(())))

      val result: Future[Result] = reviewController.onSubmit(id, updatedReviewDetail.pageUrl.drop(1), updatedReviewDetail.pageTitle, 1)(fakeRequest.withFormUrlEncodedBody(("answer", "Yes")))

      status(result) shouldBe Status.SEE_OTHER
    }

    "Return BAD_REQUEST for a post of the review completion for a process without an answer" in new Test {

      val result: Future[Result] = reviewController.onSubmit(id, updatedReviewDetail.pageUrl.drop(1), updatedReviewDetail.pageTitle, 1)(fakeRequest)

      status(result) shouldBe Status.BAD_REQUEST
    }

    "Return an Html document displaying the details of the review result" in new Test {

      MockReviewService
        .factCheckPageComplete(id, updatedReviewDetail.pageUrl, updatedReviewDetail)
        .returns(Future.successful(Right(())))

      val result: Future[Result] = reviewController.onSubmit(id, updatedReviewDetail.pageUrl.drop(1), updatedReviewDetail.pageTitle, 1)(fakeRequest.withFormUrlEncodedBody(("answer", "Yes")))

      contentType(result) shouldBe None
    }

    "Return the Http status Not found when the process review does not exist" in new Test {

      MockReviewService
        .factCheckPageComplete(id, updatedReviewDetail.pageUrl, updatedReviewDetail)
        .returns(Future.successful(Left(NotFoundError)))

      val result: Future[Result] = reviewController.onSubmit(id, updatedReviewDetail.pageUrl.drop(1), updatedReviewDetail.pageTitle, 1)(fakeRequest.withFormUrlEncodedBody(("answer", "Yes")))

      status(result) shouldBe Status.NOT_FOUND
    }

    "Return an Html error page when the process review does not exist" in new Test {

      MockReviewService
        .factCheckPageComplete(id, updatedReviewDetail.pageUrl, updatedReviewDetail)
        .returns(Future.successful(Left(NotFoundError)))

      val result: Future[Result] = reviewController.onSubmit(id, updatedReviewDetail.pageUrl.drop(1), updatedReviewDetail.pageTitle, 1)(fakeRequest.withFormUrlEncodedBody(("answer", "Yes")))

      contentType(result) shouldBe Some(MimeTypes.HTML)
    }

    "Return the Http status internal server error when the process review data returned to the application is malformed" in new Test {

      MockReviewService
        .factCheckPageComplete(id, updatedReviewDetail.pageUrl, updatedReviewDetail)
        .returns(Future.successful(Left(MalformedResponseError)))

      val result: Future[Result] = reviewController.onSubmit(id, updatedReviewDetail.pageUrl.drop(1), updatedReviewDetail.pageTitle, 1)(fakeRequest.withFormUrlEncodedBody(("answer", "Yes")))

      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "Return an Html error page when the process review data returned to the application is malformed" in new Test {

      MockReviewService
        .factCheckPageComplete(id, updatedReviewDetail.pageUrl, updatedReviewDetail)
        .returns(Future.successful(Left(MalformedResponseError)))

      val result: Future[Result] = reviewController.onSubmit(id, updatedReviewDetail.pageUrl.drop(1), updatedReviewDetail.pageTitle, 1)(fakeRequest.withFormUrlEncodedBody(("answer", "Yes")))

      contentType(result) shouldBe Some(MimeTypes.HTML)
    }

    "Return the Http status not found when the process review data requested is out of date in some way" in new Test {

      MockReviewService
        .factCheckPageComplete(id, updatedReviewDetail.pageUrl, updatedReviewDetail)
        .returns(Future.successful(Left(StaleDataError)))

      val result: Future[Result] = reviewController.onSubmit(id, updatedReviewDetail.pageUrl.drop(1), updatedReviewDetail.pageTitle, 1)(fakeRequest.withFormUrlEncodedBody(("answer", "Yes")))

      status(result) shouldBe Status.NOT_FOUND
    }

    "Return an Html error page when the process review data requested is out of date in some way" in new Test {

      MockReviewService
        .factCheckPageComplete(id, updatedReviewDetail.pageUrl, updatedReviewDetail)
        .returns(Future.successful(Left(StaleDataError)))

      val result: Future[Result] = reviewController.onSubmit(id, updatedReviewDetail.pageUrl.drop(1), updatedReviewDetail.pageTitle, 1)(fakeRequest.withFormUrlEncodedBody(("answer", "Yes")))

      contentType(result) shouldBe Some(MimeTypes.HTML)
    }

    "Return the Http status internal server error when n unexpected error occurs" in new Test {

      MockReviewService
        .factCheckPageComplete(id, updatedReviewDetail.pageUrl, updatedReviewDetail)
        .returns(Future.successful(Left(InternalServerError)))

      val result: Future[Result] = reviewController.onSubmit(id, updatedReviewDetail.pageUrl.drop(1), updatedReviewDetail.pageTitle, 1)(fakeRequest.withFormUrlEncodedBody(("answer", "Yes")))

      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "Return an Html error page when an unexpected error occurs" in new Test {

      MockReviewService
        .factCheckPageComplete(id, updatedReviewDetail.pageUrl, updatedReviewDetail)
        .returns(Future.successful(Left(InternalServerError)))

      val result: Future[Result] = reviewController.onSubmit(id, updatedReviewDetail.pageUrl.drop(1), updatedReviewDetail.pageTitle, 1)(fakeRequest.withFormUrlEncodedBody(("answer", "Yes")))

      contentType(result) shouldBe Some(MimeTypes.HTML)
    }

  }

}
