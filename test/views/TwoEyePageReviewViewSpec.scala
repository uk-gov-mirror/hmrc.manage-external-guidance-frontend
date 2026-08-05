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

import org.jsoup.nodes.Document

import play.api.data.Form

import forms.TwoEyePageReviewFormProvider
import models.forms.TwoEyePageReview

import views.html.twoeye_page_review

import scala.jdk.CollectionConverters.*

class TwoEyePageReviewViewSpec extends ViewSpecBase {

  trait Test {

    val processId: String = "ext00001"
    val reviewUrl: String = "/example"
    val reviewTitle: String = "Example page"
    val index: Int = 2

    val formProvider: TwoEyePageReviewFormProvider = new TwoEyePageReviewFormProvider()
    val form: Form[TwoEyePageReview] = formProvider()

    val two_eye_page_review: twoeye_page_review = app.injector.instanceOf[twoeye_page_review]

    val doc: Document = asDocument(two_eye_page_review(processId, reviewUrl, reviewTitle, form, index))
  }

  "The rendered two eye page review" should {

    "render a back link" in new Test {

      checkTextOnElementById(doc, "back-link", "backlink.label")
      checkAttributeOnElementById(doc, "back-link", "class", "govuk-back-link")
      checkAttributeOnElementById(doc, "back-link", "href", s"/external-guidance/2i-review/ext00001#page-link-$index")
    }

    "contain a link to the page under review" in new Test {

      Option(doc.getElementsByClass("govuk-grid-row").first).fold(fail("Unable to find govuk-grid-row")) { div =>
        val linksList = div.getElementsByTag("a").asScala.toList

        linksList.size shouldBe >(1)

        elementAttrs(linksList.head)("href") should endWith(s"/review-guidance/approval/$processId$reviewUrl")

        linksList.head.text shouldBe messages("2iPageReview.viewGuidancePage") + messages("2iPageReview.viewGuidanceHidden", reviewTitle)
      }
    }

    "contain a legend with visually hidden text" in new Test {

      Option(doc.getElementsByTag("legend").first).fold(fail("Unable to locate legend element")) { legend =>
        elementAttrs(legend)("class") shouldBe "govuk-visually-hidden"

        legend.text shouldBe messages("2iPageReview.hiddenLegend", reviewTitle)
      }
    }

  }
}
