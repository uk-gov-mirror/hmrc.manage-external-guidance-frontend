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

import java.time.ZonedDateTime

import base.BaseSpec
import connectors.httpParsers.LabelledDataHttpParser.labelledDataHttpReads
import models.{LabelledDataUpdateStatus, UpdateDetails}
import play.api.http.{HttpVerbs, Status}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpResponse
import models.errors.ValidationError
import models.errors.ForbiddenError
import models.errors.InternalServerError

class LabelledDataHttpParserSpec extends BaseSpec with HttpVerbs with Status {

  private trait Test {
    val dummyTimescales: JsValue = Json.parse("""{"TimescaleID": 10}""")
    val lastUpdateTime: ZonedDateTime = ZonedDateTime.of(2020, 1, 1, 12, 0, 1, 0, ZonedDateTime.now.getZone)
    val timescalesJson: JsValue = Json.parse("""{"First": 1, "Second": 2, "Third": 3}""")
    val timescales: Map[String, Int] = Map("First" -> 1, "Second" -> 2, "Third" -> 3)
    val credId: String = "234324234"
    val user: String = "User Blah"
    val email: String = "user@blah.com"
    val updateDetail = UpdateDetails(lastUpdateTime, "234324234", "User Blah", "user@blah.com")
    val timescalesDetail = LabelledDataUpdateStatus(timescales.size, Some(updateDetail))

    val url: String = "/test"
    val validResponse: JsValue = Json.toJson(timescalesDetail)

    val invalidResponse: JsValue = Json.obj() // no "id" property
  }

  "Parsing a successful response" should {

    "return a valid POST response" in new Test {

      private val httpResponse = HttpResponse(ACCEPTED, validResponse,  Map.empty[String, Seq[String]])
      private val result = labelledDataHttpReads.read(POST, url, httpResponse)
      result shouldBe Right(timescalesDetail)
    }

    "return a invalid POST response" in new Test {

      private val httpResponse = HttpResponse(ACCEPTED, invalidResponse,  Map.empty[String, Seq[String]])
      private val result = labelledDataHttpReads.read(POST, url, httpResponse)
      result shouldBe Left(InternalServerError)
    }

    "return a valid GET response" in new Test {

      private val httpResponse = HttpResponse(OK, validResponse,  Map.empty[String, Seq[String]])
      private val result = labelledDataHttpReads.read(GET, url, httpResponse)
      result shouldBe Right(timescalesDetail)
    }


    "return a BAD_REQUEST POST response" in new Test {

      private val httpResponse = HttpResponse(BAD_REQUEST, invalidResponse,  Map.empty[String, Seq[String]])
      private val result = labelledDataHttpReads.read(GET, url, httpResponse)
      result shouldBe Left(ValidationError)
    }


    "return a UNAUTHORIZED POST response" in new Test {

      private val httpResponse = HttpResponse(UNAUTHORIZED, invalidResponse,  Map.empty[String, Seq[String]])
      private val result = labelledDataHttpReads.read(GET, url, httpResponse)
      result shouldBe Left(ForbiddenError)
    }

  }

}
