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

package pages

import models.{PageReviewDetail, PageReviewStatus, YesNoAnswer}
import play.api.http.Status
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import play.api.libs.ws.DefaultBodyWritables.*
import play.api.libs.ws.{WSRequest, WSResponse}
import stubs.{AuditStub, AuthStub, ExternalGuidanceStub}
import support.IntegrationSpec

class FactCheckPageReviewControllerISpec extends IntegrationSpec {

  "GET /fact-check-page-review/id/pageUrl" when {

    "user is authorised" should {

      "return OK (200)" in {
        val dataReturned = PageReviewDetail("oct90005", "pageUrl", "title", Some(YesNoAnswer.Yes), PageReviewStatus.NotStarted)

        ExternalGuidanceStub.factCheckPageReview(Status.OK, Json.toJson(dataReturned))

        AuditStub.audit()
        AuthStub.authorise()

        val request: WSRequest = buildRequest("/fact-check-page-review/oct90005/pageUrl?index=2")
        val response: WSResponse = await(request.get())

        response.status shouldBe Status.OK

      }
    }

    "user not authorised" should {

      "return UNAUTHORIZED" in {

        AuditStub.audit()
        AuthStub.unauthorised()

        val request: WSRequest = buildRequest("/fact-check-page-review/oct90005/pageUrl?index=1")
        val response: WSResponse = await(request.get())

        response.status shouldBe Status.UNAUTHORIZED

      }
    }
  }

  "POST /fact-check-page-review/id/pageUrl" when {

    "user is authorised" when {

      "user selects a valid selection" should {

        "receive a redirect" in {

          AuditStub.audit()
          AuthStub.authorise()

          ExternalGuidanceStub.factCheckPageReviewComplete(Status.NO_CONTENT, Json.parse("{}"))

          val request: WSRequest = buildRequest("/fact-check-page-review/oct90005/pageUrl?title=Title&index=1")
          val response: WSResponse = await(request.post(Map("answer" -> YesNoAnswer.Yes.toString, "title" -> "Title", "index" -> 1.toString)))
          response.status shouldBe Status.SEE_OTHER
        }
      }

      "user enters an invalid selection" should {

        "return a bad request" in {

          AuditStub.audit()
          AuthStub.authorise()

          ExternalGuidanceStub.factCheckPageReviewComplete(Status.NO_CONTENT, Json.parse("{}"))

          val request: WSRequest = buildRequest("/fact-check-page-review/oct90005/pageUrl?index=1")
          val response: WSResponse = await(request.post(Json.obj("answer" -> "")))
          response.status shouldBe Status.BAD_REQUEST
        }
      }
    }
    "user not authorised" should {

      "return UNAUTHORIZED" in {

        AuditStub.audit()
        AuthStub.unauthorised()

        val request: WSRequest = buildRequest("/fact-check-page-review/oct90005/pageUrl?title=Title&index=1")
        val response: WSResponse = await(request.post(Json.obj("answer" -> YesNoAnswer.Yes.toString, "title" -> "Title", "index" -> 1)))
        response.status shouldBe Status.UNAUTHORIZED

      }
    }
  }
}
