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

package mocks

import scala.concurrent.{ExecutionContext, Future}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import services.AuditService
import models.audit.AuditEvent
import org.scalatest.TestSuite

trait MockAuditService extends TestSuite with MockFactory {

  val mockAuditService: AuditService = mock[AuditService]

  object MockAuditService {

    def audit(event: AuditEvent, path: Option[String] = None): CallHandler[Future[Unit]] = {

      (mockAuditService
        .audit(_: AuditEvent, _: Option[String])(using _: HeaderCarrier, _: ExecutionContext))
        .expects(event, path, *, *)
    }

  }

}
