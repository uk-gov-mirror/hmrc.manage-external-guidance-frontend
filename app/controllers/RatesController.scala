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

import javax.inject.{Inject, Singleton}
import models.errors.{Error, ForbiddenError, ValidationError}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.*
import controllers.actions.RatesAction
import services.RatesService
import views.html.upload_rates
import views.html.labelleddata_upload_complete
import models.{RequestOutcome, UpdateDisplayDetails, LabelledDataUpdateStatus}
import scala.concurrent.Future

@Singleton
class RatesController @Inject()(ratesService: RatesService,
                                labelledDataSecuredAction: RatesAction,
                                view: upload_rates,
                                uploadCompleteView: labelleddata_upload_complete,
                                mcc: MessagesControllerComponents) extends AbstractLabelledDataController(labelledDataSecuredAction, uploadCompleteView, mcc) {

  val dataName: String = "rates"
  def submitData(json: JsValue)(using request: Request[_]): Future[RequestOutcome[LabelledDataUpdateStatus]] = ratesService.submitRates(json)
  def getData: Action[AnyContent] = Action.async { request =>
    given Request[AnyContent] = request
    getLabelledData(ratesService.get _)
  }
  def mainPageUrl: String = controllers.routes.RatesController.home.url
  def home: Action[AnyContent] = labelledDataSecuredAction.async{ request =>
    given Request[AnyContent] = request
    uploadPage()
  }

  def uploadPage(error: Option[String] = None)(using request: Request[_]): Future[Result] =
    ratesService.details().map {
      case Right(response) =>
        val updateDisplayDetails: Option[UpdateDisplayDetails] = response.lastUpdate.map(UpdateDisplayDetails(_))
        error.fold(Ok(view(response.count, updateDisplayDetails, None)))(_ => BadRequest(view(response.count, updateDisplayDetails, error)))
      case Left(ValidationError) =>
        BadRequest(Json.toJson[Error](ValidationError))
      case Left(ForbiddenError) =>
        Unauthorized(Json.toJson[Error](ForbiddenError))
      case Left(error) =>
        logger.error(s"Rates service failure, err = $error")
        InternalServerError(Json.toJson(error))
    }

}
