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

package models.audit

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import play.api.libs.json.{JsValue, Json, Writes}
import utils.JsonObjectSugar

abstract class AuditEvent {
  val auditInfo: AuditInfo
  val detail: JsValue = Json.toJson(this)
  val auditType: String
}
object AuditEvent extends JsonObjectSugar {
  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  given writes: Writes[AuditEvent] = Writes { event =>
    jsonObjNoNulls(
      "PID" -> event.auditInfo.pid,
      "processID" -> event.auditInfo.processId,
      "submittedDate" -> LocalDate.now.format(dateFormatter),
      "processTitle" -> event.auditInfo.processTitle,
      "processVersion" -> event.auditInfo.processVersion,
      "ocelotAuthor" -> event.auditInfo.ocelotAuthor,
      "ocelotLastUpdate" -> event.auditInfo.ocelotLastUpdate,
      "ocelotVersion" -> event.auditInfo.ocelotVersion
    )
  }
}
