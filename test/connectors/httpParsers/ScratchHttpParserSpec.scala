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

package connectors.httpParsers

import java.util.UUID.randomUUID

import base.BaseSpec
import connectors.httpParsers.ScratchHttpParser.saveScratchProcessHttpReads
import models.errors.{Error, InternalServerError, InvalidProcessError, ProcessError}
import models.{RequestOutcome, ScratchResponse}
import play.api.http.{HttpVerbs, Status}
import play.api.libs.json.{JsNull, JsValue, Json, JsObject}
import uk.gov.hmrc.http.HttpResponse

class ScratchHttpParserSpec extends BaseSpec with HttpVerbs with Status {

  private trait Test {

    val url: String = "/test"

    val id: String = randomUUID().toString

    val validResponse: JsValue = Json.obj("id" -> id)

    val invalidResponse: JsValue = Json.obj() // no "id" property
  }

  "Parsing a successful response" should {

    "return a valid scratch response" in new Test {

      private val httpResponse = HttpResponse(CREATED, validResponse, Map.empty[String, Seq[String]])
      private val result = saveScratchProcessHttpReads.read(POST, url, httpResponse)
      result shouldBe Right(ScratchResponse(id))
    }
  }

  "Parsing an error response" should {

    "return an invalid process error for a bad request" in new Test {

      val httpResponse: HttpResponse = HttpResponse(BAD_REQUEST, JsNull, Map.empty[String, Seq[String]])

      val result: RequestOutcome[ScratchResponse] =
        saveScratchProcessHttpReads.read(POST, url, httpResponse)

      result shouldBe Left(InvalidProcessError)
    }

    "return an UNPROCESSABLE_ENTITY error for similar" in new Test {
      val processError: Error = Error(List(ProcessError("Duplicate page url /example-page-3 found on stanza id = 22", "22")))
      val jsObject: JsObject = Json.parse("""{"code":"UNPROCESSABLE_ENTITY","messages":[{"message":"Duplicate page url /example-page-3 found on stanza id = 22","stanza":"22"}]}""").as[JsObject]
      val httpResponse: HttpResponse = HttpResponse(UNPROCESSABLE_ENTITY, jsObject, Map.empty[String, Seq[String]])

      val result: RequestOutcome[ScratchResponse] =
        saveScratchProcessHttpReads.read(POST, url, httpResponse)

      result match {
        case Left(err) if err == processError => succeed
        case _ => fail()
      }
    }

    "return an internal server error for an invalid response" in new Test {

      val httpResponse: HttpResponse = HttpResponse(CREATED, invalidResponse, Map.empty[String, Seq[String]])

      val result: RequestOutcome[ScratchResponse] =
        saveScratchProcessHttpReads.read(POST, url, httpResponse)

      result shouldBe Left(InternalServerError)
    }

    "return an internal server error when an error distinct from invalid process occurs" in new Test {

      private val httpResponse = HttpResponse(SERVICE_UNAVAILABLE, JsNull, Map.empty[String, Seq[String]])

      val result: RequestOutcome[ScratchResponse] =
        saveScratchProcessHttpReads.read(POST, url, httpResponse)

      result shouldBe Left(InternalServerError)
    }
  }
}
