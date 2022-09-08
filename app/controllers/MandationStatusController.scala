/*
 * Copyright 2022 HM Revenue & Customs
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

import connectors.ItsaStatusConnector
import models.status.{MandationStatusRequest, MandationStatusResponse}
import models.subscription.AccountingPeriodUtil
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class MandationStatusController @Inject()(
                                           mandationStatusConnector: ItsaStatusConnector,
                                           cc: ControllerComponents
                               )(implicit ec: ExecutionContext)
  extends BackendController(cc) {

  val logger: Logger = Logger(this.getClass)

  def mandationStatus: Action[JsValue] = Action.async(parse.json) { implicit request  =>
    withJsonBody[MandationStatusRequest] { requestBody =>
      mandationStatusConnector.getItsaStatus(requestBody.nino, requestBody.utr).map {
        case Right(response) => {
          val maybeCurrentTaxYearStatus = response.taxYearStatus.find(_.taxYear.equals(AccountingPeriodUtil.getCurrentTaxYear.toShortTaxYear))
          val maybeNextTaxYearStatus = response.taxYearStatus.find(_.taxYear.equals(AccountingPeriodUtil.getNextTaxYear.toShortTaxYear))

          (maybeCurrentTaxYearStatus, maybeNextTaxYearStatus) match {
            case (Some(currentTaxYearStatus), Some(nextTaxYearStatus)) => {
              Ok(Json.toJson(MandationStatusResponse(currentTaxYearStatus.status, nextTaxYearStatus.status)))
            }
            case _ => InternalServerError("Failed to retrieve the mandation status")
          }
        }
        case Left(error) => {
          logger.error(s"Error processing mandation status request with status ${error.status} and message ${error.reason}")
          InternalServerError("Failed to retrieve the mandation status")
        }
      }
    }
  }

}
