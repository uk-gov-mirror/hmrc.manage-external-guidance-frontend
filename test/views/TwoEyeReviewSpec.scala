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

import java.time.*
import java.util.UUID
import models.PageReviewStatus._
import models._
import org.jsoup.nodes.Document
import views.html._

import scala.jdk.CollectionConverters._

class TwoEyeReviewSpec extends ViewSpecBase {

  trait Test {
    def twoEyeReview: twoeye_content_review = injector.instanceOf[twoeye_content_review]

    val approvalProcessReview: ApprovalProcessReview = ApprovalProcessReview(
      UUID.randomUUID().toString,
      "oct9005",
      "Telling HMRC about extra income",
      LocalDate.of(2020, 5, 10),
      List(
        PageReview("id1", "/how-did-you-earn-extra-income", "title1", Complete, Some("Yes")),
        PageReview("id2", "/sold-goods-or-services/did-you-only-sell-personal-possessions", "title2", NotStarted, None),
        PageReview("id3", "/sold-goods-or-services/have-you-made-a-profit-of-6000-or-more", "title3", NotStarted, None),
        PageReview("id4", "/sold-goods-or-services/have-you-made-1000-or-more", "title4", NotStarted, None),
        PageReview("id5", "/sold-goods-or-services/you-do-not-need-to-tell-hmrc", "title5", NotStarted, None),
        PageReview("id6", "/rent-a-property/do-you-receive-any-income", "title6", NotStarted, None),
        PageReview("id7", "/rent-a-property/have-you-rented-out-a-room", "title7", NotStarted, None)
      )
    )
    val doc: Document = asDocument(twoEyeReview(approvalProcessReview))
  }

  "2i Review page" should {
    "Render a page should display process title as the heading" in new Test {
      Option(doc.getElementsByTag("h1").first).fold(fail("Missing H1 heading of the correct class")) { h1 =>
        elementAttrs(h1)("class") should include("govuk-heading-xl")
        h1.text shouldBe approvalProcessReview.title
      }
    }

    "Render header with caption paragraph" in new Test {

      Option(doc.getElementsByTag("main").first).fold(fail("Unable to locate main element in view")) { main =>
        Option(main.getElementsByTag("header").first).fold(fail("Unable to locate header in main element")) { header =>
          header.children.size shouldBe 2

          header.child(0).tagName shouldBe "h1"

          header.child(1).tagName shouldBe "p"
          header.child(1).text shouldBe messages("2iReview.heading")
        }
      }
    }

    "Include a back link" in new Test {

      checkTextOnElementById(doc, "back-link", "backlink.label")
      checkAttributeOnElementById(doc, "back-link", "class", "govuk-back-link")
      checkAttributeOnElementById(doc, "back-link", "href", "/external-guidance")
    }

    "Render a page containing all listing all of files and their status" in new Test {

      Option(doc.getElementsByTag("ul").first).fold(fail("Missing ul element")) { ul =>
        val listItems = ul
          .getElementsByTag("li")
          .asScala
          .filter(elementAttrs(_).get("class") == Some("app-task-list__item"))
          .toList

        listItems.zipWithIndex.foreach {
          case (_, index) =>
            val a = doc.getElementById(s"page-link-$index")
            approvalProcessReview.pages.find(_.title == a.text).fold(fail(s"Missing page with title ${a.text}")) { page =>
              messages(s"2i.pageReviewStatus.${page.status.toString}${page.result.toString}") shouldBe doc.getElementById(s"page-$index").text
            }
        }
      }
    }
  }

}
