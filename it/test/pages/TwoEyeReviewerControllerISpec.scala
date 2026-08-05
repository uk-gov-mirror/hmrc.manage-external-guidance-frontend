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

import models.errors.{Error, BadRequestError, InternalServerError, NotFoundError, StaleDataError, IncompleteDataError}
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import play.api.libs.ws.{WSRequest, WSResponse}
import models.ApprovalStatus
import models.audit.AuditInfo
import stubs.{AuditStub, AuthStub, ExternalGuidanceStub}
import support.IntegrationSpec

class TwoEyeReviewerControllerISpec extends IntegrationSpec {

  private val successPayload: JsValue = Json.parse(
    """{
      |  "id":"oct90005",
      |  "ocelotId":"oct90005",
      |  "title":"Telling HMRC about extra income",
      |  "lastUpdated":"2020-05-10",
      |  "pages":[
      |    {
      |      "id":"id1",
      |      "url":"how-did-you-earn-extra-income",
      |      "title":"how-did-you-earn-extra-income",
      |      "status":"NotStarted"
      |    },
      |    {
      |      "id":"id2",
      |      "title":"sold-goods-or-services/did-you-only-sell-personal-possessions",
      |      "url":"sold-goods-or-services/did-you-only-sell-personal-possessions",
      |      "status":"NotStarted"
      |    },
      |    {
      |      "id":"id3",
      |      "title":"sold-goods-or-services/have-you-made-a-profit-of-6000-or-more",
      |      "url":"sold-goods-or-services/have-you-made-a-profit-of-6000-or-more",
      |      "status":"NotStarted"
      |    },
      |    {
      |      "id":"id4",
      |      "title":"sold-goods-or-services/have-you-made-1000-or-more",
      |      "url":"sold-goods-or-services/have-you-made-1000-or-more",
      |      "status":"NotStarted"
      |    },
      |    {
      |      "id":"id5",
      |      "title":"sold-goods-or-services/you-do-not-need-to-tell-hmrc",
      |      "url":"sold-goods-or-services/you-do-not-need-to-tell-hmrc",
      |      "status":"NotStarted"}
      |    ,
      |    {
      |      "id":"id6",
      |      "title":"rent-a-property/do-you-receive-any-income",
      |      "url":"rent-a-property/do-you-receive-any-income",
      |      "status":"NotStarted"
      |    },
      |    {
      |      "id":"id7",
      |      "title":"rent-a-property/have-you-rented-out-a-room",
      |      "url":"rent-a-property/have-you-rented-out-a-room",
      |      "status":"NotStarted"
      |    }
      |  ]
      |}""".stripMargin
  )

  "GET the approval 2i review endpoint with a valid process identifier" should {

    "return an Ok response" in {

      AuditStub.audit()
      AuthStub.authorise()

      ExternalGuidanceStub.approval2iReview(Status.OK, successPayload)

      val request: WSRequest = buildRequest("/2i-review/oct90005")

      val response: WSResponse = await(request.get())

      response.status shouldBe Status.OK
    }

    "return a not found response when the external guidance service cannot locate the requested process" in {

      AuditStub.audit()
      AuthStub.authorise()

      val responsePayload: JsValue = Json.toJson[Error](NotFoundError)

      ExternalGuidanceStub.approval2iReview(Status.NOT_FOUND, responsePayload)

      val request = buildRequest("/2i-review/oct90005")

      val response: WSResponse = await(request.get())

      response.status shouldBe Status.NOT_FOUND
    }

    "return an internal server error response when an unexpected error is returned by the external guidance service" in {

      AuditStub.audit()
      AuthStub.authorise()

      val responsePayload: JsValue = Json.toJson[Error](InternalServerError)

      ExternalGuidanceStub.approval2iReview(Status.INTERNAL_SERVER_ERROR, responsePayload)

      val request = buildRequest("/2i-review/oct90005")

      val response: WSResponse = await(request.get())

      response.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return a not found error response when the external guidance service returns a stale data error" in {

      AuditStub.audit()
      AuthStub.authorise()

      val responsePayload: JsValue = Json.toJson[Error](StaleDataError)

      ExternalGuidanceStub.approval2iReview(Status.NOT_FOUND, responsePayload)

      val request = buildRequest("/2i-review/oct90005")

      val response: WSResponse = await(request.get())

      response.status shouldBe Status.NOT_FOUND
    }

    "return an internal server error response when the external guidance service returns a bad request error" in {

      AuditStub.audit()
      AuthStub.authorise()

      val responsePayload: JsValue = Json.toJson[Error](BadRequestError)

      ExternalGuidanceStub.approval2iReview(Status.BAD_REQUEST, responsePayload)

      val request = buildRequest("/2i-review/oct90005")

      val response: WSResponse = await(request.get())

      response.status shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return an UNAUTHORIZED response for an unauthorized user" in {

      AuditStub.audit()
      AuthStub.unauthorised()

      ExternalGuidanceStub.approval2iReview(Status.OK, successPayload)

      val request: WSRequest = buildRequest("/2i-review/oct90005")

      val response: WSResponse = await(request.get())

      response.status shouldBe Status.UNAUTHORIZED
    }

  }

  "POST /2i-review/id" when {

    "user is authorised" when {
      "user selects to send guidance to designer" should {
        "receive a confirmation page" in {
          AuditStub.audit()
          AuthStub.authorise()
          val auditInfo: AuditInfo = AuditInfo("pid", "oct90005", "title", 1, "author", 2, 2)
          ExternalGuidanceStub.approval2iReviewCheck(Status.NO_CONTENT, Json.parse("{}"))          
          ExternalGuidanceStub.approval2iReviewComplete(Status.OK, Json.toJsObject(auditInfo))

          val request: WSRequest = buildRequest("/2i-review/oct90005")
          val response: WSResponse = await(request.withMethod("POST").post(Json.obj("value" -> ApprovalStatus.Published.toString)))
          response.status shouldBe Status.OK
        }
      }
      "user selects to publish guidance" should {
        "receive a confirmation page" in {
          AuditStub.audit()
          AuthStub.authorise()
          val auditInfo: AuditInfo = AuditInfo("pid", "oct90005", "title", 1, "author", 2, 2)
          ExternalGuidanceStub.approval2iReviewCheck(Status.NO_CONTENT, Json.parse("{}"))
          ExternalGuidanceStub.approval2iReviewComplete(Status.OK, Json.toJsObject(auditInfo))

          val request: WSRequest = buildRequest("/2i-review/oct90005")
          val response: WSResponse = await(request.withMethod("POST").post(Json.obj("value" -> ApprovalStatus.Published.toString)))
          response.status shouldBe Status.OK
        }
      }

      "user enters an invalid selection" should {

        "return a bad request" in {

          AuditStub.audit()
          AuthStub.authorise()
          ExternalGuidanceStub.approval2iReviewCheck(Status.BAD_REQUEST, Json.toJson[Error](IncompleteDataError))
          ExternalGuidanceStub.approval2iReviewComplete(Status.BAD_REQUEST, Json.parse("{}"))

          val request: WSRequest = buildRequest("/2i-review/oct90005")
          val response: WSResponse = await(request.post(Json.obj("value" -> ApprovalStatus.Submitted.toString)))
          response.status shouldBe Status.BAD_REQUEST
        }
      }
    }
    "user not authorised" should {

      "return UNAUTHORIZED" in {

        AuditStub.audit()
        AuthStub.unauthorised()

        val request: WSRequest = buildRequest("/2i-review/oct90005")
        val response: WSResponse = await(request.post(Json.obj("value" -> ApprovalStatus.Complete.toString)))
        response.status shouldBe Status.UNAUTHORIZED

      }
    }
  }

}
