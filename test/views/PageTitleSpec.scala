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

import java.time.LocalDate

import forms.{FactCheckPageReviewFormProvider, TwoEyePageReviewFormProvider}
import models.ApprovalProcessReview
import org.jsoup.nodes.Document
import play.api.data.FormError
import play.api.test.FakeRequest
import views.html.*

class PageTitleSpec extends ViewSpecBase {

  val formErrors = Seq(FormError("id", "Err"))

  def expectedTitleText(h1Text: String, section: Option[String] = None): String =
    section.fold(s"$h1Text - ${messages("service.name")} - ${messages("service.govuk")}"){s =>
      s"$h1Text - $s${titleSuffix()}"
    }

  def checkTitle(doc: Document, section: Option[String] = None, prefix: Option[String] = None): Unit =
    Option(doc.getElementsByTag("h1").first).fold(fail("Missing H1")){ h1 =>
      Option(doc.getElementsByTag("title").first).fold(fail("Missing title")){title =>
        prefix.fold(title.text shouldBe expectedTitleText(h1.text, section)){ prefx =>
          title.text shouldBe s"$prefx ${expectedTitleText(h1.text, section)}"
        }
      }
    }

  def checkTitleWithError(doc: Document, section: Option[String] = None): Unit =
    checkTitle(doc, section, Some(messages(messages("error.prefix"))))

  "Page rendering" should {

    "Render correct approvals approval_summary_list title" in {
      val view = injector.instanceOf[approval_summary_list]
      checkTitle(asDocument(view(Nil)(FakeRequest("GET", "/blah"), messages)))
    }

    "Render correct accessibility title" in {
      val view = injector.instanceOf[accessibility_statement]
      checkTitle(asDocument(view()(FakeRequest("GET", "/blah"), messages)))
    }

    "Render correct fact check complete title" in {
      val view = injector.instanceOf[fact_check_complete]
      checkTitle(asDocument(view()(FakeRequest("GET", "/blah"), messages)), Some(messages("factCheck.heading")))
    }

    "Render correct fact check confirm error title" in {
      val view = injector.instanceOf[fact_check_confirm_error]
      checkTitle(asDocument(view("error")(FakeRequest("GET", "/blah"), messages)), Some(messages("factCheck.heading")))
    }

    "Render correct fact check content review title" in {
      val view = injector.instanceOf[fact_check_content_review]
      checkTitle(asDocument(view(ApprovalProcessReview("","","TITLE", LocalDate.now, Nil))(FakeRequest("GET", "/blah"), messages)), Some(messages("factCheck.heading")))
    }

    "Render correct fact_check_page_review title" in {
      val view = injector.instanceOf[fact_check_page_review]
      val formProvider = new FactCheckPageReviewFormProvider()
      checkTitle(asDocument(view("", "", "", formProvider(), 1)(FakeRequest("GET", "/blah"), messages)), Some(messages("factCheck.heading")))
    }

    "Render correct fact_check_page_review title when error has occurred" in {
      val view = injector.instanceOf[fact_check_page_review]
      val formProvider = new FactCheckPageReviewFormProvider()
      val form = formProvider().copy( errors = formErrors)
      checkTitleWithError(asDocument(view("","", "", form, 1)(FakeRequest("GET", "/blah"), messages)), Some(messages("factCheck.heading")))
    }

    "Render correct twoeye_confirm_error title" in {
      val view = injector.instanceOf[twoeye_confirm_error]
      checkTitle(asDocument(view("processId")(FakeRequest("GET", "/blah"), messages)), Some(messages("2iReview.heading")))
    }

    "Render correct twoeye_content_review title" in {
      val view = injector.instanceOf[twoeye_content_review]
      checkTitle(asDocument(view(ApprovalProcessReview("","","TITLE", LocalDate.now, Nil))(FakeRequest("GET", "/blah"), messages)), Some(messages("2iReview.heading")))
    }
    
    "Render correct twoeye_page_review title" in {
      val view = injector.instanceOf[twoeye_page_review]
      val formProvider = new TwoEyePageReviewFormProvider()
      checkTitle(asDocument(view("","", "", formProvider(), 1)(FakeRequest("GET", "/blah"), messages)), Some(messages("2iReview.heading")))
    }

    "Render correct twoeye_page_review title when error has occurred" in {
      val view = injector.instanceOf[twoeye_page_review]
      val formProvider = new TwoEyePageReviewFormProvider()
      val form = formProvider().copy( errors = formErrors)
      checkTitleWithError(asDocument(view("","", "", form, 1)(FakeRequest("GET", "/blah"), messages)), Some(messages("2iReview.heading")))
    }

  }
}