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

import java.util.UUID

import play.api.http.Status
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import play.api.libs.ws.WSResponse
import play.api.libs.json.*
import models.errors.{Error, InternalServerError, InvalidProcessError}
import stubs.{AuditStub, ExternalGuidanceStub}
import support.IntegrationSpec

class SaveScratchISpec extends IntegrationSpec {

  "calling the scratch POST endpoint" should {
    "return an CREATED response" in {

      AuditStub.audit()

      val responsePayload = Json.obj("id" -> UUID.randomUUID().toString)
      ExternalGuidanceStub.saveScratch(Status.CREATED, responsePayload)

      val request = buildRequest("/process/scratch")
      val response: WSResponse = await(request.post(validOnePageJson))
      response.status shouldBe Status.CREATED
    }

    "return a BAD_REQUEST response when the external guidance microservice rejects with BadRequest" in {

      AuditStub.audit()

      val responsePayload: JsValue = Json.toJson[Error](InvalidProcessError)
      ExternalGuidanceStub.saveScratch(Status.BAD_REQUEST, responsePayload)

      val request = buildRequest("/process/scratch")
      val response: WSResponse = await(request.post(validOnePageJson))
      response.status shouldBe Status.BAD_REQUEST
    }

    "return an INTERNAL_SERVER_ERROR response when the external guidance microservice returns an internal server error" in {

      AuditStub.audit()

      val responsePayload: JsValue = Json.toJson[Error](InternalServerError)
      ExternalGuidanceStub.saveScratch(Status.INTERNAL_SERVER_ERROR, responsePayload)

      val request = buildRequest("/process/scratch")
      val response: WSResponse = await(request.post(Json.obj("message" -> "hi")))
      response.status shouldBe Status.INTERNAL_SERVER_ERROR
    }
  }

  "calling the scratch OPTIONS endpoint" should {
    "return an OK response" in {
      AuditStub.audit()
      val request = buildRequest("/process/scratch")
      val response: WSResponse = await(request.options())
      response.status shouldBe Status.OK
    }
  }

}
