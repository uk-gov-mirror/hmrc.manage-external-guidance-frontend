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

import models._
import models.audit.AuditInfo
import models.errors._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsNull, JsValue, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse

class ReviewHttpParserSpec extends AnyWordSpec with Matchers with ReviewData {

  import connectors.httpParsers.ReviewHttpParser.given
  
  class GetReviewDetailsSetup(status: Int, json: JsValue) {
    private val httpMethod = "GET"
    private val url = "/"
    private val httpResponse = HttpResponse(status, json, Map.empty[String, Seq[String]])

    def readResponse: RequestOutcome[ApprovalProcessReview] =
      getReviewDetailsHttpReads.read(httpMethod, url, httpResponse)
  }

  "The ReviewHttpParser.getReviewDetailsHttpReads" when {
    "the response is OK" when {
      "the json successfully deserializes to the model" should {
        "Return ApprovalProcessReview" in new GetReviewDetailsSetup(OK, Json.toJson(reviewInfo)) {
          readResponse shouldBe Right(reviewInfo)
        }
      }
      "the json is malformed" should {
        "return MalformedResponseError" in new GetReviewDetailsSetup(OK, JsNull) {
          readResponse shouldBe Left(MalformedResponseError)
        }
      }
    }
    "the response is NOT_FOUND with a code of NOT_FOUND_ERROR" should {
      "return NotFoundError" in new GetReviewDetailsSetup(NOT_FOUND, Json.obj("code" -> "NOT_FOUND_ERROR", "message" -> "")) {
        readResponse shouldBe Left(NotFoundError)
      }
    }
    "the response is NOT_FOUND with a code of STALE_DATA_ERROR" should {
      "return StaleDataError" in new GetReviewDetailsSetup(NOT_FOUND, Json.obj("code" -> "STALE_DATA_ERROR", "message" -> "")) {
        readResponse shouldBe Left(StaleDataError)
      }
    }
    "the response is NOT_FOUND and has malformed error json" should {
      "return InternalServerError" in new GetReviewDetailsSetup(NOT_FOUND, Json.obj("message" -> "NOT_FOUND_ERROR")) {
        readResponse shouldBe Left(InternalServerError)
      }
    }
    "the response is BAD_REQUEST with a code of DUPLICATE_KEY_ERROR" should {
      "return StaleDataError" in new GetReviewDetailsSetup(BAD_REQUEST, Json.obj("code" -> "DUPLICATE_KEY_ERROR", "message" -> "")) {
        readResponse shouldBe Left(DuplicateKeyError)
      }
    }
    "the response is anything else" should {
      "return InternalServerError" in new GetReviewDetailsSetup(INTERNAL_SERVER_ERROR, Json.obj("code" -> "INTERNAL_SERVER_ERROR", "message" -> "")) {
        readResponse shouldBe Left(InternalServerError)
      }
    }
  }

  class PostReviewCompleteSetup(status: Int, json: JsValue) {
    private val httpMethod = "GET"
    private val url = "/"
    private val httpResponse = HttpResponse(status, json, Map.empty[String, Seq[String]])

    def readResponse: RequestOutcome[Unit] =
      postReviewCompleteHttpReads.read(httpMethod, url, httpResponse)
  }

  "The ReviewHttpParser.postReviewCompleteHttpReads" when {
    "the response is NO_CONTENT" should {
      "Return Unit" in new PostReviewCompleteSetup(NO_CONTENT, JsNull) {
        readResponse shouldBe Right(())
      }
    }
    "the response is NOT_FOUND with a code of NOT_FOUND_ERROR" should {
      "return NotFoundError" in new PostReviewCompleteSetup(NOT_FOUND, Json.obj("code" -> "NOT_FOUND_ERROR", "message" -> "")) {
        readResponse shouldBe Left(NotFoundError)
      }
    }
    "the response is INCOMPLETE_DATA_ERROR" should {
      "return IncompleteDataError" in new PostReviewCompleteSetup(BAD_REQUEST, Json.obj("code" -> "INCOMPLETE_DATA_ERROR", "message" -> "")) {
        readResponse shouldBe Left(IncompleteDataError)
      }
    }
    "the response is NOT_FOUND with a code of STALE_DATA_ERROR" should {
      "return StaleDataError" in new PostReviewCompleteSetup(NOT_FOUND, Json.obj("code" -> "STALE_DATA_ERROR", "message" -> "")) {
        readResponse shouldBe Left(StaleDataError)
      }
    }
    "the response is NOT_FOUND and has malformed error json" should {
      "return InternalServerError" in new PostReviewCompleteSetup(NOT_FOUND, Json.obj("message" -> "NOT_FOUND_ERROR")) {
        readResponse shouldBe Left(InternalServerError)
      }
    }
    "the response is anything else" should {
      "return InternalServerError" in new PostReviewCompleteSetup(INTERNAL_SERVER_ERROR, Json.obj("code" -> "INTERNAL_SERVER_ERROR", "message" -> "")) {
        readResponse shouldBe Left(InternalServerError)
      }
    }
  }

  "The ReviewHttpParser.getReviewPageDetailsHttpReads" when {
    val pageUrl = "pageUrl"
    val pageReviewDetail = PageReviewDetail(id, pageUrl, "Title", None, PageReviewStatus.NotStarted)

    class GetReviewPageDetailsSetup(status: Int, json: JsValue) {
      private val httpMethod = "GET"
      private val url = "/"
      private val httpResponse = HttpResponse(status, json, Map.empty[String, Seq[String]])

      def readResponse: RequestOutcome[PageReviewDetail] =
        getReviewPageDetailsHttpReads.read(httpMethod, url, httpResponse)
    }

    "the response is OK" when {
      "the json successfully deserializes to the model" should {
        "Return ApprovalProcessReview" in new GetReviewPageDetailsSetup(OK, Json.toJson(pageReviewDetail)) {
          readResponse shouldBe Right(pageReviewDetail)
        }
      }
      "the json is malformed" should {
        "return MalformedResponseError" in new GetReviewPageDetailsSetup(OK, JsNull) {
          readResponse shouldBe Left(MalformedResponseError)
        }
      }
    }
    "the response is NOT_FOUND with a code of NOT_FOUND_ERROR" should {
      "return NotFoundError" in new GetReviewPageDetailsSetup(NOT_FOUND, Json.obj("code" -> "NOT_FOUND_ERROR", "message" -> "")) {
        readResponse shouldBe Left(NotFoundError)
      }
    }
    "the response is NOT_FOUND with a code of STALE_DATA_ERROR" should {
      "return StaleDataError" in new GetReviewPageDetailsSetup(NOT_FOUND, Json.obj("code" -> "STALE_DATA_ERROR", "message" -> "")) {
        readResponse shouldBe Left(StaleDataError)
      }
    }
    "the response is NOT_FOUND and has malformed error json" should {
      "return InternalServerError" in new GetReviewPageDetailsSetup(NOT_FOUND, Json.obj("message" -> "NOT_FOUND_ERROR")) {
        readResponse shouldBe Left(InternalServerError)
      }
    }
    "the response is anything else" should {
      "return InternalServerError" in new GetReviewPageDetailsSetup(INTERNAL_SERVER_ERROR, Json.obj("code" -> "INTERNAL_SERVER_ERROR", "message" -> "")) {
        readResponse shouldBe Left(InternalServerError)
      }
    }
  }

  "The ReviewHttpParser.getAuditInfoHttpReads" when {
    val ocelotLastUpdate: Long = 12345678
    val auditInfo: AuditInfo = AuditInfo("pid", id, "title", 1, "author", ocelotLastUpdate, 2)

    class TestSetup(status: Int, json: JsValue) {
      private val httpMethod = "GET"
      private val url = "/"
      private val httpResponse = HttpResponse(status, json, Map.empty[String, Seq[String]])

      def readResponse: RequestOutcome[AuditInfo] =
        getAuditInfoHttpReads.read(httpMethod, url, httpResponse)
    }

    "the response is OK" when {
      "the json successfully deserializes to the model" should {
        "Return AuditInfo" in new TestSetup(OK, Json.toJson(auditInfo)) {
          readResponse shouldBe Right(auditInfo)
        }
      }
      "the json is malformed" should {
        "return MalformedResponseError" in new TestSetup(OK, JsNull) {
          readResponse shouldBe Left(MalformedResponseError)
        }
      }
    }
    "the response is BAD_REQUEST" should {
      "return BadRequestError" in new TestSetup(BAD_REQUEST, Json.toJson(BadRequestError : models.errors.Error)) {
        readResponse shouldBe Left(BadRequestError)
      }
    }
    "the response is NOT_FOUND with a code of NOT_FOUND_ERROR" should {
      "return NotFoundError" in new TestSetup(NOT_FOUND, Json.toJson(NotFoundError : models.errors.Error)) {
        readResponse shouldBe Left(NotFoundError)
      }
    }
    "the response is NOT_FOUND with a code of STALE_DATA_ERROR" should {
      "return StaleDataError" in new TestSetup(NOT_FOUND, Json.toJson(StaleDataError : models.errors.Error)) {
        readResponse shouldBe Left(StaleDataError)
      }
    }
    "the response is NOT_FOUND and has malformed error json" should {
      "return InternalServerError" in new TestSetup(NOT_FOUND, Json.obj("message" -> "NOT_FOUND_ERROR")) {
        readResponse shouldBe Left(InternalServerError)
      }
    }
    "the response is anything else" should {
      "return InternalServerError" in new TestSetup(INTERNAL_SERVER_ERROR, Json.obj("code" -> "INTERNAL_SERVER_ERROR", "message" -> "")) {
        readResponse shouldBe Left(InternalServerError)
      }
    }
  }

}
