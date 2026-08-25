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

package controllers

import controllers.actions.LabelledDataAction
import models.{LabelledDataUpdateStatus, RequestOutcome, UpdateDisplayDetails}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.*
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.labelleddata_upload_complete

import java.nio.file.*
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

abstract class AbstractLabelledDataController (
                                      labelledDataSecuredAction: LabelledDataAction,
                                      uploadComplete: labelleddata_upload_complete,
                                      mcc: MessagesControllerComponents) extends FrontendController(mcc) with I18nSupport with Logging {
  given ec: ExecutionContext = mcc.executionContext

  def getLabelledData(dataService: () => Future[RequestOutcome[JsValue]]): Future[Result] =
    dataService().map {
      case Right(json) => Ok(json)
      case Left(error) =>
        logger.error(s"LabelledData service failure, err = $error")
        InternalServerError(Json.toJson(error))
    }

  val dataName: String
  def mainPageUrl: String
  def submitData(json: JsValue)(using request: Request[_]): Future[RequestOutcome[LabelledDataUpdateStatus]]
  def uploadPage(error: Option[String])(using request: Request[_]): Future[Result]

  def upload: Action[play.api.mvc.MultipartFormData[play.api.libs.Files.TemporaryFile]] =
    labelledDataSecuredAction.async(parse.multipartFormData) { request =>
      given  Request[play.api.mvc.MultipartFormData[play.api.libs.Files.TemporaryFile]] = request
      request.body.file(dataName) match {
        case Some(data) if data.contentType.fold(false)(_.contains("json")) =>
          readJsonFile(data.ref.path) match {
            case Success(json) =>
              submitData(json).flatMap {
                case Right(response) =>
                  Future.successful(Ok(uploadComplete(response.count, response.lastUpdate.map(UpdateDisplayDetails(_)), dataName, mainPageUrl)))
                case Left(err) =>
                  logger.error(s"Failed to submit labelled data update, err = $err")
                  uploadPage(Some(s"${dataName}.error.invalid"))
              }
            case Failure(err) =>
              logger.warn(s"Selected labelled data update file is invalid, err = $err")
              uploadPage(Some(s"${dataName}.error.invalid"))
          }
        case Some(_) => uploadPage(Some(s"${dataName}.error.notjson"))
        case _ => uploadPage(Some(s"${dataName}.error.noselection"))
      }
    }

  private def readJsonFile(uploadFile: Path): Try[JsValue] =
    Try(Files.newInputStream(uploadFile, StandardOpenOption.READ)).flatMap{f =>
      val result = Try(Json.parse(f))
      f.close()
      result
    }
}
