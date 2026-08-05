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
import connectors.httpParsers.PublishedProcessHttpParser.publishedProcessHttpReads
import connectors.httpParsers.PublishedProcessHttpParser.processHttpReads
import connectors.httpParsers.PublishedProcessHttpParser.processSummaryHttpReads
import models.errors.{Error, InternalServerError, NotFoundError, InvalidProcessError, ProcessError}

import models.{PublishedProcess, RequestOutcome}
import play.api.http.{HttpVerbs, Status}
import play.api.libs.json.{JsNull, JsObject, JsValue, Json, Format}
import uk.gov.hmrc.http.HttpResponse

import java.time.ZonedDateTime
import java.util.UUID.randomUUID
import models.ProcessSummary

class PublishedProcessHttpParserSpec extends BaseSpec with HttpVerbs with Status {

  private trait Test {

    val date: ZonedDateTime = ZonedDateTime.now()

    val url: String = "/test"

    val id: String = randomUUID().toString

    val validResponse: JsValue = Json.obj(
      "id" -> "id",
      "version" -> 1,
      "datePublished" -> date,
      "process" -> Json.obj("a" -> "b"),
      "publishedBy" -> "user",
      "processCode" -> "code"
    )

    val invalidResponse: JsValue = Json.obj() // no "id" property

    val validJsonResponse: JsValue = Json.obj()

    val published: PublishedProcess = PublishedProcess(
      "id",
      1,
      date,
      Json.obj("a" -> "b"),
      "user",
      "code"
    )

    given formats: Format[Error] = Json.format[Error]
    val notFoundErrorJson = Json.toJson(Error("NOT_FOUND_ERROR", Some("The resource requested could not be found."), None))

    val processSummary: ProcessSummary = ProcessSummary("id", "code", 1, "author", None, ZonedDateTime.now, "actionedby", "Status")
  }

  "Parsing a successful response" should {

    "return a valid published process response" in new Test {

      private val httpResponse = HttpResponse(OK, validResponse, Map.empty[String, Seq[String]])
      private val result = publishedProcessHttpReads.read(POST, url, httpResponse)
      result shouldBe Right(published)
    }

    "return a valid published process JsValue response" in new Test {

      private val httpResponse = HttpResponse(OK, validJsonResponse, Map.empty[String, Seq[String]])
      private val result = processHttpReads.read(POST, url, httpResponse)
      result shouldBe Right(validJsonResponse)
    }

    "return a invalid published process JsValue response" in new Test {

      private val httpResponse = HttpResponse(OK, "", Map.empty[String, Seq[String]])
      private val result = processHttpReads.read(POST, url, httpResponse)
      result shouldBe Left(InternalServerError)
    }

    "return a Bad request response" in new Test {

      private val httpResponse = HttpResponse(BAD_REQUEST, "", Map.empty[String, Seq[String]])
      private val result = processHttpReads.read(POST, url, httpResponse)
      result shouldBe Left(InvalidProcessError)
    }

    "return a not found response" in new Test {

      private val httpResponse = HttpResponse(NOT_FOUND, notFoundErrorJson, Map.empty[String, Seq[String]])
      private val result = processHttpReads.read(POST, url, httpResponse)
      result shouldBe Left(NotFoundError)
    }

    "return a valid ProcessSummary list response" in new Test {

      val data: JsValue = Json.toJson(List(processSummary))

      private val httpResponse = HttpResponse(OK, data, Map.empty[String, Seq[String]])
      private val result = processSummaryHttpReads.read(POST, url, httpResponse)
      result shouldBe Right(List(processSummary))
    }

    "return a invalid ProcessSummary list response" in new Test {
      private val httpResponse = HttpResponse(OK, "", Map.empty[String, Seq[String]])
      private val result = processSummaryHttpReads.read(POST, url, httpResponse)
      result shouldBe Left(InternalServerError)
    }

    "return a BadRequest list response" in new Test {
      private val httpResponse = HttpResponse(BAD_REQUEST, "", Map.empty[String, Seq[String]])
      private val result = processSummaryHttpReads.read(POST, url, httpResponse)
      result shouldBe Left(InvalidProcessError)
    }

    "return a Not found  list response" in new Test {
      private val httpResponse = HttpResponse(NOT_FOUND, notFoundErrorJson, Map.empty[String, Seq[String]])
      private val result = processSummaryHttpReads.read(POST, url, httpResponse)
      result shouldBe Left(NotFoundError)
    }

  }

  "Parsing an error response" should {

    "return an invalid process error for a bad request" in new Test {

      val httpResponse: HttpResponse = HttpResponse(BAD_REQUEST, JsNull, Map.empty[String, Seq[String]])

      val result: RequestOutcome[PublishedProcess] =
        publishedProcessHttpReads.read(POST, url, httpResponse)

      result shouldBe Left(InvalidProcessError)
    }

    "return an UNPROCESSABLE_ENTITY error for similar" in new Test {
      val processError: Error = Error(List(ProcessError("Duplicate page url /example-page-3 found on stanza id = 22", "22")))
      val jsObject: JsObject = Json.parse("""{"code":"UNPROCESSABLE_ENTITY","messages":[{"message":"Duplicate page url /example-page-3 found on stanza id = 22","stanza":"22"}]}""").as[JsObject]
      val httpResponse: HttpResponse = HttpResponse(UNPROCESSABLE_ENTITY, jsObject, Map.empty[String, Seq[String]])

      val result: RequestOutcome[PublishedProcess] =
        publishedProcessHttpReads.read(POST, url, httpResponse)

      result match {
        case Left(err) if err == processError => succeed
        case _ => fail()
      }
    }

    "return an internal server error for an invalid response" in new Test {

      val httpResponse: HttpResponse = HttpResponse(CREATED, invalidResponse, Map.empty[String, Seq[String]])

      val result: RequestOutcome[PublishedProcess] =
        publishedProcessHttpReads.read(POST, url, httpResponse)

      result shouldBe Left(InternalServerError)
    }

    "return an internal server error when invalid process occurs" in new Test {

      private val httpResponse = HttpResponse(OK, "", Map.empty[String, Seq[String]])

      val result: RequestOutcome[PublishedProcess] =
        publishedProcessHttpReads.read(POST, url, httpResponse)

      result shouldBe Left(InternalServerError)
    }

    "return an internal server error when an error distinct from invalid process occurs" in new Test {

      private val httpResponse = HttpResponse(SERVICE_UNAVAILABLE, JsNull, Map.empty[String, Seq[String]])

      val result: RequestOutcome[PublishedProcess] =
        publishedProcessHttpReads.read(POST, url, httpResponse)

      result shouldBe Left(InternalServerError)
    }
  }
}
