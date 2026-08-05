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


import base.BaseSpec
import connectors.httpParsers.ActiveProcessHttpParser.{processHttpReads, summaryHttpReads}
import java.time.Instant
import play.api.http.{HttpVerbs, Status}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpResponse
import models.errors.{NotFoundError, BadRequestError, InternalServerError}
import models.admin.CachedProcessSummary

class ActiveProcessHttpParserSpec extends BaseSpec with HttpVerbs with Status {

  private trait Test {
    val url: String = "/test"
    val validResponse: JsValue = Json.obj("id" -> "ext90111")
  }

  "Parsing response a GET process" should {

    "return a valid GET response" in new Test {

      private val httpResponse = HttpResponse(OK, validResponse,  Map.empty[String, Seq[String]])
      private val result = processHttpReads.read(GET, url, httpResponse)
      result shouldBe Right(validResponse)
    }

    "return a invalid valid GET response" in new Test {

      private val httpResponse = HttpResponse(OK, "",  Map.empty[String, Seq[String]])
      private val result = processHttpReads.read(GET, url, httpResponse)
      result shouldBe Left(InternalServerError)
    }

    "return a BadRequest GET response" in new Test {

      private val httpResponse = HttpResponse(BAD_REQUEST, validResponse,  Map.empty[String, Seq[String]])
      private val result = processHttpReads.read(GET, url, httpResponse)
      result shouldBe Left(BadRequestError)
    }

    "return a not found GET response" in new Test {

      private val httpResponse = HttpResponse(NOT_FOUND, validResponse,  Map.empty[String, Seq[String]])
      private val result = processHttpReads.read(GET, url, httpResponse)
      result shouldBe Left(NotFoundError)
    }
  }

  private trait ListTest {
    val url: String = "/test"
    val oneItemList: List[CachedProcessSummary] = List(CachedProcessSummary("id", 123456789L, None, None, "A title", Instant.now))
    val emptyList: List[CachedProcessSummary] = Nil
    val validResponseNil: JsValue = Json.toJson(emptyList)
    val validResponse: JsValue = Json.toJson(oneItemList)
  }

  "Parsing response a GET List[CachedProcessSummary]" should {

    "return a valid no records response" in new ListTest {

      private val httpResponse = HttpResponse(OK, validResponseNil,  Map.empty[String, Seq[String]])
      private val result = summaryHttpReads.read(GET, url, httpResponse)
      result shouldBe Right(emptyList)
    }

    "return a valid List of one record response" in new ListTest {

      private val httpResponse = HttpResponse(OK, validResponse,  Map.empty[String, Seq[String]])
      private val result = summaryHttpReads.read(GET, url, httpResponse)
      result shouldBe Right(oneItemList)
    }

    "return a invalid List of one record response" in new ListTest {

      private val httpResponse = HttpResponse(OK, "",  Map.empty[String, Seq[String]])
      private val result = summaryHttpReads.read(GET, url, httpResponse)
      result shouldBe Left(InternalServerError)
    }

    "return a error GET response" in new ListTest {

      private val httpResponse = HttpResponse(NOT_FOUND, validResponse,  Map.empty[String, Seq[String]])
      private val result = summaryHttpReads.read(GET, url, httpResponse)
      result shouldBe Left(InternalServerError)
    }
  }

}
