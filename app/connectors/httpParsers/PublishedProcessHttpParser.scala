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

import models.errors.{InternalServerError, InvalidProcessError}
import models.{PublishedProcess, RequestOutcome, ProcessSummary}
import play.api.Logger
import play.api.http.Status.*
import uk.gov.hmrc.http.HttpReads
import play.api.libs.json.JsValue

object PublishedProcessHttpParser extends HttpParser {

  val logger: Logger = Logger(getClass)

  given publishedProcessHttpReads: HttpReads[RequestOutcome[PublishedProcess]] = {
    case (_, _, response) if response.status == OK =>
      response.validateJson[PublishedProcess] match {
        case Some(result) => Right(result)
        case None =>
          logger.error("Unable to parse successful response when reading published process.")
          Left(InternalServerError)
      }
    case (_, _, response) if response.status == BAD_REQUEST => Left(InvalidProcessError)
    case (_, _, response) => Left(response.checkErrorResponse)
  }

  given processHttpReads: HttpReads[RequestOutcome[JsValue]] = {
    case (_, _, response) if response.status == OK =>
      response.validateJson[JsValue] match {
        case Some(result) => Right(result)
        case None =>
          logger.error("Unable to parse successful response when reading published process.")
          Left(InternalServerError)
      }
    case (_, _, response) if response.status == BAD_REQUEST => Left(InvalidProcessError)
    case (_, _, response) => Left(response.checkErrorResponse)
  }

  given processSummaryHttpReads: HttpReads[RequestOutcome[List[ProcessSummary]]] = {
    case (_, _, response) if response.status == OK =>
      response.validateJson[List[ProcessSummary]] match {
        case Some(result) => Right(result)
        case None =>
          logger.error("Unable to parse successful response when reading published process.")
          Left(InternalServerError)
      }
    case (_, _, response) if response.status == BAD_REQUEST => Left(InvalidProcessError)
    case (_, _, response) => Left(response.checkErrorResponse)
  }

}
