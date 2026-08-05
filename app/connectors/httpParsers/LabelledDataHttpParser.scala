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

import models.errors.{ForbiddenError, ValidationError, InternalServerError}
import models.RequestOutcome
import play.api.Logger
import play.api.http.Status.*
import uk.gov.hmrc.http.HttpReads
import models.LabelledDataUpdateStatus

object LabelledDataHttpParser extends HttpParser {
  val logger: Logger = Logger(getClass)

  given labelledDataHttpReads: HttpReads[RequestOutcome[LabelledDataUpdateStatus]] = {
    case (_, _, response) if response.status == OK || response.status == ACCEPTED =>
      response.validateJson[LabelledDataUpdateStatus] match {
        case Some(response)  => Right(response)
        case None =>
          logger.error("Unable to parse successful response when POSTing LabelledData update")
          Left(InternalServerError)
      }
    case (_, _, response) if response.status == BAD_REQUEST => Left(ValidationError)
    case (_, _, response) if response.status == UNAUTHORIZED => Left(ForbiddenError)
    case (_, _, response) => Left(response.checkErrorResponse)
  }

}
