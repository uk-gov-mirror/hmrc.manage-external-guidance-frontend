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

package views.components

import org.jsoup.nodes.Element
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.FormError
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.inject.Injector
import play.twirl.api.Html
import views.ViewSpecBase
import views.html.components.yesno_radios

import scala.jdk.CollectionConverters._

class YesNoRadiosSpec extends ViewSpecBase with GuiceOneAppPerSuite {

  val pageUrl = "/url"
  val pageTitle = "Some Page Title"
  val processId = "ext90087"
  val fieldName = "field"
  val heading = "Some Heading"
  val linkText = "Link Text"

  trait Test {
    private def injector: Injector = app.injector
    def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]
    given messages: Messages = messagesApi.preferred(Seq(Lang("en")))
  }

  "yesno_radios generated Html" must {

    def radios: yesno_radios = injector.instanceOf[yesno_radios]
    val html: Html = radios(None, Seq.empty, pageTitle, fieldName)
    val doc = asDocument(html)
    val inputs: List[Element] = doc.getElementsByTag("input").asScala.toList

    "contains fieldset two radio inputs" in new Test {
      inputs.size shouldBe 2
      inputs.map { inp =>
        elementAttrs(inp)("type") shouldBe "radio"
      }
    }
  }

  "yesno_radios generated Html when error has occurred" must {

    def radios: yesno_radios = injector.instanceOf[yesno_radios]
    val html: Html = radios(None, Seq(FormError("Key", "Error message")), pageTitle, fieldName)
    val doc = asDocument(html)
    val fieldset: Element = doc.getElementsByTag("fieldset").first

    "contain fieldset with appropriate aria-describedby link to error msg span" in new Test {
      Option(fieldset).fold(fail("No fieldset found")) { fs =>
        elementAttrs(fs)("aria-describedby").contains(s"$fieldName-error") shouldBe true
      }
    }

    "contain a span containing the error message and prefix" in new Test {
      Option(doc.getElementById(s"$fieldName-error")).fold(fail("Missing error message")) { span =>
        span.text shouldBe "Error: Error message"
      }
    }
  }

}
