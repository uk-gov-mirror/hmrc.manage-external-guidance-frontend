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

package endpoints

import models.errors.{Error, InternalServerError, InvalidProcessError}
import play.api.http.Status
import play.api.libs.json.*
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import play.api.libs.ws.WSResponse
import stubs.{AuditStub, AuthStub, ExternalGuidanceStub}
import support.IntegrationSpec

class ApprovalControllerISpec extends IntegrationSpec {

  private val endPoint2iReview = "/process/approval/2i-review"
  private val endPointFactCheck = "/process/approval/fact-check"

  "calling the approval 2i Review POST endpoint" should {
    "return an CREATED response" in {

      AuditStub.audit()
      AuthStub.authorise()

      val responsePayload = Json.obj("id" -> "oct90001")
      ExternalGuidanceStub.save2iReview(Status.CREATED, responsePayload)

      val request = buildRequest(endPoint2iReview)
      val response: WSResponse = await(request.withMethod("POST").post(validOnePageJson))
      response.status shouldBe Status.CREATED
    }

    "return a BAD_REQUEST response when the external guidance microservice returns a BadRequest" in {

      AuditStub.audit()
      AuthStub.authorise()

      val responsePayload: JsValue = Json.toJson[Error](InvalidProcessError)
      ExternalGuidanceStub.save2iReview(Status.BAD_REQUEST, responsePayload)

      val request = buildRequest(endPoint2iReview)
      val response: WSResponse = await(request.withMethod("POST").post(validOnePageJson))
      response.status shouldBe Status.BAD_REQUEST
    }

    "return an INTERNAL_SERVER_ERROR response when the external guidance microservice returns an internal server error" in {

      AuditStub.audit()

      val responsePayload: JsValue = Json.toJson[Error](InternalServerError)
      ExternalGuidanceStub.save2iReview(Status.INTERNAL_SERVER_ERROR, responsePayload)

      val request = buildRequest(endPoint2iReview)
      val response: WSResponse = await(request.withMethod("POST").post(validOnePageJson))
      response.status shouldBe Status.INTERNAL_SERVER_ERROR
    }
  }

  "calling the approval Fact Check POST endpoint" should {
    "return an CREATED response" in {

      AuditStub.audit()

      val responsePayload = Json.obj("id" -> "oct90001")
      ExternalGuidanceStub.saveFactCheck(Status.CREATED, responsePayload)

      val request = buildRequest(endPointFactCheck)
      val response: WSResponse = await(request.withMethod("POST").post(validOnePageJson))
      response.status shouldBe Status.CREATED
    }

    "return a BAD_REQUEST response when the external guidance microservice returns a BadRequest" in {

      AuditStub.audit()

      val responsePayload: JsValue = Json.toJson[Error](InvalidProcessError)
      ExternalGuidanceStub.saveFactCheck(Status.BAD_REQUEST, responsePayload)

      val request = buildRequest(endPointFactCheck)
      val response: WSResponse = await(request.post(validOnePageJson))
      response.status shouldBe Status.BAD_REQUEST
    }

    "return an INTERNAL_SERVER_ERROR response when the external guidance microservice returns an internal server error" in {

      AuditStub.audit()

      val responsePayload: JsValue = Json.toJson[Error](InternalServerError)
      ExternalGuidanceStub.saveFactCheck(Status.INTERNAL_SERVER_ERROR, responsePayload)

      val request = buildRequest(endPointFactCheck)
      val response: WSResponse = await(request.post(validOnePageJson))
      response.status shouldBe Status.INTERNAL_SERVER_ERROR
    }
  }

  "calling the approval OPTIONS endpoint" should {
    "return an OK response" in {
      AuditStub.audit()
      val request = buildRequest(endPoint2iReview)
      val response: WSResponse = await(request.options())
      response.status shouldBe Status.OK
    }
  }

}
