/*
 * Copyright 2020 HM Revenue & Customs
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

import base.BaseSpec
import models.errors.{InternalServerError, InvalidProcessError}
import play.api.http.{HttpVerbs, Status}
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.http.HttpResponse

class HttpParserSpec extends BaseSpec with HttpVerbs with Status with HttpParser {

  case class Person(name: String, age: Int)

  object Person {
    implicit val formats: OFormat[Person] = Json.format[Person]
  }

  "Calling validateJson with a valid JSON response" should {
    "return the model associated with the JSON" in {
      val model = Person("Bob", 1)
      val json = Json.toJson(model)
      val response = HttpResponse(OK, Some(json))
      response.validateJson[Person] shouldBe Some(model)
    }
  }

  "Calling validateJson with no response" should {
    "return None" in {
      val response = HttpResponse(OK)
      response.validateJson[Person] shouldBe None
    }
  }

  "Calling validateJson with an invalid JSON response" should {
    "return None" in {
      val json = Json.obj()
      val response = HttpResponse(OK, Some(json))
      response.validateJson[Person] shouldBe None
    }
  }

  "Calling checkErrorResponse with a valid JSON response" should {
    "return InvalidProcessError" in {
      val json = Json.obj("code" -> "BAD_REQUEST", "message" -> "")
      val response = HttpResponse(BAD_REQUEST, Some(json))
      response.checkErrorResponse shouldBe InvalidProcessError
    }
  }

  "Calling checkErrorResponse with empty JSON response" should {
    "return InternalServerError" in {
      val json = Json.obj()
      val response = HttpResponse(BAD_REQUEST, Some(json))
      response.checkErrorResponse shouldBe InternalServerError
    }
  }
}
