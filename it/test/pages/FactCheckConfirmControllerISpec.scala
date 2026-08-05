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

import models.audit.AuditInfo
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import stubs.{AuditStub, AuthStub, ExternalGuidanceStub}
import support.IntegrationSpec

class FactCheckConfirmControllerISpec extends IntegrationSpec {

  private val request: WSRequest = buildRequest("/fact-check-result/oct90005")

  "POST /fact-check-result/id" when {
    "user is authorised" when {
      "process is in a state to be confirmed" should {
        "display the complete view" in {
          AuditStub.audit()
          AuthStub.authorise()

          val auditInfo: AuditInfo = AuditInfo("pid", "oct90005", "title", 1, "author", 2, 2)
          ExternalGuidanceStub.factCheckComplete(OK, Json.toJson(auditInfo))

          val response: WSResponse = await(request.get())
          response.status shouldBe OK
        }
      }
    }
    "user not authorised" should {
      "return UNAUTHORIZED" in {
        AuditStub.audit()
        AuthStub.unauthorised()

        val response: WSResponse = await(request.get())
        response.status shouldBe UNAUTHORIZED

      }
    }
  }
}
