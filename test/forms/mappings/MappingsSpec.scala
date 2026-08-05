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

package forms.mappings

import models.Enumerable

import play.api.data.{Form, FormError}

import base.BaseSpec

import org.scalatest.OptionValues

object MappingsSpec {

  sealed trait A

  case object B extends A
  case object C extends A

  object A {

    val values: Set[A] = Set(B, C)

    given aEnumerable: Enumerable[A] = Enumerable(values.toSeq.map(v => v.toString -> v): _*)
  }

}

class MappingsSpec extends BaseSpec with OptionValues with Mappings {

  import MappingsSpec.*

  "enumerable" should {

    val testForm = Form(
      "value" -> enumerable[A]()
    )

    "bind a valid option" in {
      val result = testForm.bind(Map("value" -> "B"))
      result.get shouldEqual B
    }

    "not bind an invalid option" in {
      val result = testForm.bind(Map("value" -> "Not B"))
      result.errors should contain(FormError("value", "error.invalid"))
    }

    "not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors should contain(FormError("value", "error.required"))
    }

    "unbind a valid option" in {
      val result = testForm.bind(Map("value" -> "B"))
      result.get shouldEqual B
    }
  }

}
