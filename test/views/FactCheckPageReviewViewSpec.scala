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

import forms.FactCheckPageReviewFormProvider
import models.forms.FactCheckPageReview
import org.jsoup.nodes.Document
import play.api.data.Form
import views.html.fact_check_page_review

import scala.jdk.CollectionConverters.*

class FactCheckPageReviewViewSpec extends ViewSpecBase {

  trait Test {

    val processId = "ext00001"
    val reviewUrl = "/example"
    val reviewTitle = "Example page"
    val index = 0

    val formProvider: FactCheckPageReviewFormProvider = new FactCheckPageReviewFormProvider()
    val form: Form[FactCheckPageReview] = formProvider()

    val fact_check_page_review: fact_check_page_review = app.injector.instanceOf[fact_check_page_review]

    val doc: Document = asDocument(fact_check_page_review(processId, reviewUrl, reviewTitle, form, index))
  }

  "The rendered fact check page review" should {

    "Render a back link" in new Test {

      checkTextOnElementById(doc, "back-link", "backlink.label")
      checkAttributeOnElementById(doc, "back-link", "class", "govuk-back-link")
      checkAttributeOnElementById(doc, "back-link", "href", s"/external-guidance/fact-check/ext00001#page-link-$index")
    }

    "contain a link to the page under review" in new Test {

      Option(doc.getElementsByClass("govuk-grid-row").first).fold(fail("Unable to find govuk-grid-row")) { div =>
        val linksList = div.getElementsByTag("a").asScala.toList

        linksList.size shouldBe >(1)

        elementAttrs(linksList(0))("href") should endWith(s"/review-guidance/approval/$processId$reviewUrl")

        linksList(0).text shouldBe messages("factCheckPageReview.viewGuidancePage") + messages("factCheckPageReview.viewGuidancePageVisuallyHidden", reviewTitle)
      }
    }

    "contain a legend with visually hidden text" in new Test {

      Option(doc.getElementsByTag("legend").first).fold(fail("Unable to locate legend element")) { legend =>
        elementAttrs(legend)("class") shouldBe "govuk-visually-hidden"

        legend.text shouldBe messages("factCheckPageReview.hiddenHeading", reviewTitle)
      }
    }
  }
}
