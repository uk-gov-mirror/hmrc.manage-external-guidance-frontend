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

import controllers.actions.FakeAllRolesAction
import mocks.MockApprovalService
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import views.html.approval_summary_list

import scala.concurrent.Future

class AdminControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with MockApprovalService {

  private trait Test {

    private val view = app.injector.instanceOf[approval_summary_list]
    lazy val errorHandler = app.injector.instanceOf[config.ErrorHandler]
    given hc: HeaderCarrier = HeaderCarrier()
    val fakeRequest = FakeRequest("GET", "/")
    val controller = new AdminController(FakeAllRolesAction, errorHandler, view, mockApprovalService, stubMessagesControllerComponents())

  }

  "GET /" should {

    "return 200" in new Test {

      MockApprovalService.approvalSummaries.returns(Future.successful(Right(List())))

      val result = controller.approvalSummaries(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "return HTML" in new Test {

      MockApprovalService.approvalSummaries.returns(Future.successful(Right(List())))

      val result = controller.approvalSummaries(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }

}
