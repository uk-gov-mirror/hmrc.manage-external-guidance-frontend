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

import models.errors.*
import play.api.Logger
import play.api.libs.json.*
import uk.gov.hmrc.http.HttpResponse

import scala.util.{Success, Try}

trait HttpParser {

  private val logger: Logger = Logger(HttpParser.this.getClass)

  extension (response: HttpResponse) {
    def validateJson[T](using reads: Reads[T]): Option[T] = {
      Try(response.json) match {
        case Success(json: JsValue) => parseResult(json)
        case _ =>
          logger.error("Unable to retrieve JSON from response")
          None
      }
    }

    private def parseResult[T](json: JsValue)(using reads: Reads[T]): Option[T] = {
      json.validate[T] match {
        case JsSuccess(value, _) => Some(value)
        case JsError(error) =>
          logger.error(s"Unable to parse JSON in response: $error")
          None
      }
    }

    def checkErrorResponse: Error = {
      validateJson[Error] match {
        case Some(expectedError) => expectedError.code match {
          case NotFoundError.code => NotFoundError
          case StaleDataError.code => StaleDataError
          case DuplicateKeyError.code => DuplicateKeyError
          case UpgradeRequiredError.code => UpgradeRequiredError
          case IncompleteDataError.code => IncompleteDataError
          case BadRequestError.code => BadRequestError
          case InvalidProcessError.code => InvalidProcessError
          case Error.UnprocessableEntity => expectedError
          case _ => InternalServerError
        }
        case None =>
          logger.error(s"Unable to parse error response.")
          InternalServerError
      }
    }
  }

}
