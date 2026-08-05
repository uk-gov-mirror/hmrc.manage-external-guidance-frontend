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
import java.time.format.DateTimeFormatter
import controllers.routes
import models.ApprovalStatus._
import models.ReviewType.{ReviewType2i, ReviewTypeFactCheck}
import models._
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.html._

import scala.jdk.CollectionConverters._

class ApprovalsListSpec extends ViewSpecBase {

  trait Test {
    def approvalsListView: approval_summary_list = injector.instanceOf[approval_summary_list]

    val summaries = Seq(
      ApprovalProcessSummary("oct9005", "EU exit guidance", LocalDate.of(2020, 5, 4), Complete, ReviewType2i, 1),
      ApprovalProcessSummary("oct9006", "Customer wants to make a cup of tea", LocalDate.of(2020, 5, 4), Submitted, ReviewType2i, 1),
      ApprovalProcessSummary("oct9007", "Telling HMRC about extra income", LocalDate.of(2020, 4, 1), Complete, ReviewType2i, 1),
      ApprovalProcessSummary("oct9008", "Find a lost user ID and password", LocalDate.of(2020, 4, 2), Submitted, ReviewTypeFactCheck, 1)
    )

    given fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")

    val doc: Document = asDocument(approvalsListView(summaries)(fakeRequest, messages))
  }

  "Approval Summary List page" should {

    "Render header with service name link" in new Test {

      val headers: Elements = doc.getElementsByTag("header")

      headers.size() shouldBe 1

      val anchors: Elements = headers.first.getElementsByTag("a")

      anchors.size shouldBe 2

      Option(anchors.last).fold(fail("Service Url link not found")) { a =>
        a.text shouldBe messages("service.name")

        elementAttrs(a).get("href").fold(fail("Missing href attribute in anchor")) { href =>
          href shouldBe routes.AdminController.approvalSummaries.url
        }
      }
    }

    "Render with correct heading" in new Test {
      Option(doc.getElementsByTag("h1").first).fold(fail("Missing H1 heading of the correct class")) { h1 =>
        elementAttrs(h1)("class") should include("govuk-heading-xl")
        h1.text shouldBe messages("approvals.tableTitle")
      }
    }

    "Contain a table with correct headings" in new Test {

      Option(doc.getElementsByTag("table").first).fold(fail("Missing table elem")) { table =>
        val ths = table.getElementsByTag("th").asScala.toList
        ths.size shouldBe 5
        ths(0).text shouldBe messages("approvals.processTitle")
        elementAttrs(ths(0)).get("class").fold(fail("Missing class on table col header"))(_ should include("govuk-table__header"))

        ths(1).text shouldBe messages("approvals.dateProcessUpdatedTitle")
        elementAttrs(ths(1)).get("class").fold(fail("Missing class on table col header"))(_ should include("govuk-table__header"))

        ths(2).text shouldBe messages("approvals.version")
        elementAttrs(ths(1)).get("class").fold(fail("Missing class on table col header"))(_ should include("govuk-table__header"))

        ths(3).text shouldBe messages("approvals.processId")
        elementAttrs(ths(1)).get("class").fold(fail("Missing class on table col header"))(_ should include("govuk-table__header"))

        ths(4).text shouldBe messages("approvals.processStatusTitle")
        elementAttrs(ths(2)).get("class").fold(fail("Missing class on table col header"))(_ should include("govuk-table__header"))
      }
    }

    "define a link to the general comments and feedback page" in new Test {

      Option(doc.getElementsByClass("govuk-grid-column-two-thirds").first).fold(fail("Missing layout grid")) { gridDiv =>
        Option(gridDiv.getElementsByTag("a").first).fold(fail("No anchor elements found in layout grid")) { a =>
          a.text shouldBe messages("approvals.commentsAndFeedback")

          elementAttrs(a).get("class").fold(fail("Missing class attribute on comments and feedback link")) { clss =>
            clss shouldBe "govuk-link"
          }
        }
      }

    }

    "include a table entry for each approval summary in order" in new Test {

      Option(doc.getElementsByTag("tbody").first).fold(fail("Missing table body")) { tbody =>
        val rows = tbody.getElementsByTag("tr").asScala.toList

        rows.size shouldBe summaries.size

        rows.zip(summaries).foreach {
          case (r, s) =>
            val cellData = r.getElementsByTag("td").asScala.toList

            cellData.size shouldBe 5

            s.status match {

              case Submitted | InProgress =>
                cellData.head.text shouldBe Seq(messages("approvals.processTitle"), s.title).mkString(" ")

                Option(cellData.head.getElementsByTag("a").first).fold(fail("Missing link from page url cell")) { a =>
                  elementAttrs(a).get("href").fold(fail("Missing href attribute within anchor")) { href =>
                    (s.reviewType: @unchecked) match {
                      case ReviewType2i if List(InProgress, Submitted).contains(s.status) => href shouldBe routes.TwoEyeReviewController.approval(s.id).url
                      case ReviewTypeFactCheck if List(InProgress, Submitted).contains(s.status) => href shouldBe routes.FactCheckController.approval(s.id).url
                    }
                  }
                }
              case _ => {
                // Cell data contains hidden column title plus actual process title
                cellData.head.text shouldBe Seq(messages("approvals.processTitle"), s.title).mkString(" ")
              }
            }

            val lastUpdatedCellText: String = s.lastUpdated.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
            cellData(1).text shouldBe Seq(messages("approvals.dateProcessUpdatedTitle"), lastUpdatedCellText).mkString(" ")

            val versionCellText: String = s.version.toString
            cellData(2).text shouldBe Seq(messages("approvals.version"), versionCellText).mkString(" ")

            val idCellText: String = s.id
            cellData(3).text shouldBe Seq(messages("approvals.processId"), idCellText).mkString(" ")

            val statusCellText: String = messages(s"approvalsStatus.${s.reviewType.toString}.${s.status.toString}")
            cellData(4).text shouldBe Seq(messages("approvals.processStatusTitle"), statusCellText).mkString(" ")
        }
      }
    }

    "Render link to contact frontend page help" in new Test {

      Option(doc.getElementById("getpagehelp-link")).fold(fail("Test cannot locate contact frontend page help link")) { a =>
        a.text shouldBe messages("getPageHelp.linkText")

        elementAttrs(a).get("href").fold(fail("Test cannot locate href attribute in contact frontend page help link")) { href =>
          href shouldBe appConfig.reportAProblemNonJSUrl
        }
      }
    }

    "render list of processes for approval as a responsive table" in new Test {

      val tables: List[Element] = doc.getElementsByTag("table").asScala.toList

      tables.size shouldBe >(0)

      // Check CSS classes on table element
      elementAttrs(tables.head).get("class").fold(fail("Unable to find classes assigned to HTML table element")) { cls =>
        cls shouldBe "govuk-table hmrc-responsive-table"
      }

      // Check role assigned to table element
      elementAttrs(tables.head).get("role").fold(fail("Unable to find role assigned to HTML table element")) { role =>
        role shouldBe "table"
      }

      val theads: List[Element] = tables.head.getElementsByTag("thead").asScala.toList

      theads.size shouldBe 1

      // Check CSS classes on thead element
      elementAttrs(theads.head).get("class").fold(fail("Unable to find class assigned to HTML thead element")) { cls =>
        cls shouldBe "govuk-table__head"
      }

      // Check role assigned to thead element
      elementAttrs(theads.head).get("role").fold(fail("Unable to find role assigned to HTML thead element")) { role =>
        role shouldBe "rowgroup"
      }

      val headerRows: List[Element] = theads.head.getElementsByTag("tr").asScala.toList

      headerRows.size shouldBe 1

      elementAttrs(headerRows.head).get("class").fold(fail("Unable to find class assigned to HTML table header row element")) { cls =>
        cls shouldBe "govuk-table__row"
      }

      elementAttrs(headerRows.head).get("role").fold(fail("Unable to find role assigned to HTML table header row element")) { role =>
        role shouldBe "row"
      }

      val headerCells: List[Element] = headerRows.head.getElementsByTag("th").asScala.toList

      headerCells.size shouldBe 5

      for (headerCell <- headerCells) {

        elementAttrs(headerCell).get("scope").fold(fail("Unable to find scope assigned to HTML table header cell")) { scope =>
          scope shouldBe "col"
        }

        elementAttrs(headerCell).get("class").fold(fail("Unable to find class assigned to HTML table header cell")) { cls =>
          cls shouldBe "govuk-table__header"
        }

        elementAttrs(headerCell).get("role").fold(fail("Unable to find role assigned to HTML table header cell")) { role =>
          role shouldBe "columnheader"
        }
      }

      val tbodies: List[Element] = tables.head.getElementsByTag("tbody").asScala.toList

      tbodies.size shouldBe 1

      elementAttrs(tbodies.head).get("class").fold(fail("Unable to find class on HTML table body element")) { cls =>
        cls shouldBe "govuk-table__body"
      }

      val bodyRows: List[Element] = tbodies.head.getElementsByTag("tr").asScala.toList

      bodyRows.size shouldBe 4

      for (bodyRow <- bodyRows) {

        elementAttrs(bodyRow).get("class").fold(fail("Unable to find class assigned to HTML table body row")) { cls =>
          cls shouldBe "govuk-table__row"
        }

        elementAttrs(bodyRow).get("role").fold(fail("Unable to find role assigned to HTML table body row")) { role =>
          role shouldBe "row"
        }

        val dataCells = bodyRow.getElementsByTag("td").asScala.toList

        dataCells.size shouldBe 5

        for (dataCell <- dataCells) {

          elementAttrs(dataCell).get("class").fold(fail("Unable to find class assigned to HTML table data cell")) { cls =>
            cls shouldBe "govuk-table__cell"
          }

          elementAttrs(dataCell).get("role").fold(fail("Unable to find role assigned to HTML table data cell")) { role =>
            role shouldBe "cell"
          }

          // Check span inside data cell
          val spans = dataCell.getElementsByTag("span").asScala.toList

          spans.size shouldBe 1

          elementAttrs(spans.head).get("class").fold(fail("Unable to find class assigned to span within table data cell")) { cls =>
            cls shouldBe "hmrc-responsive-table__heading"
          }

          elementAttrs(spans.head).get("aria-hidden").fold(fail("Unable to find artia-hidden attribute on HMTL data cell")) { attribute =>
            attribute shouldBe "true"
          }

        }
      }
    }
  }
}
