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

package views

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import scala.jdk.CollectionConverters.*
import play.api.inject.Injector
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.twirl.api.Html
import config.AppConfig
import base.BaseSpec
import org.scalatest.Assertion
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest

trait ViewSpecBase extends BaseSpec with GuiceOneAppPerSuite {

  def injector: Injector = app.injector

  def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  given fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")

  given messages: Messages = messagesApi.preferred(fakeRequest)

  given appConfig: AppConfig = injector.instanceOf[AppConfig]

  def titleSuffix(): String = s" - ${messages("service.name")} - ${messages("service.govuk")}"

  def asDocument(html: Html): Document = Jsoup.parse(html.toString())

  def assertEqualsMessage(doc: Document, cssSelector: String, expectedMessageKey: String): Assertion =
    assertEqualsValue(doc, cssSelector, messages(expectedMessageKey))

  def assertEqualsValue(doc: Document, cssSelector: String, expectedValue: String): Assertion = {

    val elements = doc.select(cssSelector)

    if (elements.isEmpty) throw new IllegalArgumentException(s"CSS Selector $cssSelector wasn't rendered.")

    //<p> HTML elements are rendered out with a carriage return on some pages, so discount for comparison
    assert(elements.first().html().replace("\n", "") == expectedValue)
  }

  def assertTextEqualsMessage(docText: String, expectedMessageKey: String, args: Any*): Assertion = {

    docText.replaceAll("\u00a0", " ") shouldBe messages(expectedMessageKey, args: _*).replaceAll("&nbsp;", " ")
  }

  def assertPageHeadingEqualsMessage(doc: Document, expectedMessageKey: String, args: Any*): Assertion = {
    val headers = doc.getElementsByTag("h1")
    headers.size shouldBe 1
    headers.first.text.replaceAll("\u00a0", " ") shouldBe messages(expectedMessageKey, args: _*).replaceAll("&nbsp;", " ")
  }

  def assertParagraphTextEqualsMessage(doc: Document, paragraphIndex: Int, expectedMessageKey: String, args: Any*): Assertion = {

    val paragraphs = doc.getElementsByTag("p")

    if (paragraphIndex < 0 || paragraphIndex > paragraphs.size()) {
      throw new IndexOutOfBoundsException("Paragraph index exceeds number of paragraphs found on page")
    }

    paragraphs.get(paragraphIndex - 1).text().replaceAll("\u00a0", " ") shouldBe messages(expectedMessageKey, args: _*).replaceAll("&nbsp;", " ")
  }

  def elementAttrs(el: Element): Map[String, String] = el.attributes.asScala.toList.map(attr => (attr.getKey, attr.getValue)).toMap

  def checkTextOnElementById(doc: Document, elementId: String, expectedMessageKey: String, args: Any*): Unit = {
    val optionElement: Option[Element] = Option(doc.getElementById(elementId))
    optionElement match {
      case Some(tag) => assertTextEqualsMessage(tag.text, expectedMessageKey, args)
      case _ => fail()
    }
  }

  def checkAttributeOnElementById(doc: Document, elementId: String, attribute: String, expectedValue: String): Unit = {
    val optionElement: Option[Element] = Option(doc.getElementById(elementId))
    optionElement match {
      case Some(tag) =>
        val attrs = elementAttrs(tag)
        attrs.get(attribute).fold(fail(s"Missing $attribute attribute")) { attr =>
          attr shouldBe expectedValue
        }
      case _ => fail()
    }
  }
}
