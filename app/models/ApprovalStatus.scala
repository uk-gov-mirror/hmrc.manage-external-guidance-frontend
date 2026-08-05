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

package models

sealed trait ProcessStatus

sealed trait ApprovalStatus extends ProcessStatus

object ApprovalStatus extends Enumerable.Implicits {

  case object Submitted extends WithName("Submitted") with ApprovalStatus
  case object InProgress extends WithName("InProgress") with ApprovalStatus
  case object Complete extends WithName("Complete") with ApprovalStatus
  case object Published extends WithName("Published") with ApprovalStatus
  case object Archived extends WithName("Archived") with ApprovalStatus

  val values: Seq[ApprovalStatus] = Seq(Submitted, InProgress, Complete, Published, Archived)

  val inProgress: Seq[ApprovalStatus] = Seq(Submitted, InProgress)

  given enumerable: Enumerable[ApprovalStatus] = Enumerable(values.map(v => v.toString -> v): _*)

}
