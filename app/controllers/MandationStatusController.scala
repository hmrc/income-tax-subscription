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

import common.Extractors
import config.AppConfig
import config.featureswitch.{FeatureSwitching, UseHIPForItsaStatus}
import connectors.ItsaStatusConnector
import connectors.hip.HipItsaStatusConnector
import models.monitoring.MandationStatusAuditModel
import models.status.{ITSAStatus, MandationStatusRequest, MandationStatusResponse}
import models.subscription.AccountingPeriodUtil
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import services.AuthService
import services.monitoring.AuditService
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MandationStatusController @Inject()(
                                           authService: AuthService,
                                           auditService: AuditService,
                                           mandationStatusConnector: ItsaStatusConnector,
                                           hipItsaStatusConnector: HipItsaStatusConnector,
                                           cc: ControllerComponents,
                                           val appConfig: AppConfig
                                         )(implicit ec: ExecutionContext)
  extends BackendController(cc) with Extractors with FeatureSwitching {

  val logger: Logger = Logger(this.getClass)

  def mandationStatus: Action[JsValue] = Action.async(parse.json) { implicit request =>
    authService.authorised().retrieve(Retrievals.allEnrolments) { enrolments =>
      withJsonBody[MandationStatusRequest] { requestBody =>

        val statusResult: Future[(Option[ITSAStatus], Option[ITSAStatus])] = if (isEnabled(UseHIPForItsaStatus)) {
          hipItsaStatusConnector.getItsaStatus(requestBody.nino, requestBody.utr) map {
            case Right(response) =>
              val current = response.taxYearStatus.find(_.taxYear == AccountingPeriodUtil.getCurrentTaxYear.toItsaStatusShortTaxYear).map(_.status)
              val next = response.taxYearStatus.find(_.taxYear == AccountingPeriodUtil.getNextTaxYear.toItsaStatusShortTaxYear).map(_.status)
              (current, next)
            case Left(_) =>
              throw new InternalServerException("[MandationStatusController] - Failure response fetching mandation status.")
          }
        } else {
          mandationStatusConnector.getItsaStatus(requestBody.nino, requestBody.utr) map {
            case Right(response) =>
              val current = response.taxYearStatus.find(_.taxYear == AccountingPeriodUtil.getCurrentTaxYear.toItsaStatusShortTaxYear).map(_.status)
              val next = response.taxYearStatus.find(_.taxYear == AccountingPeriodUtil.getNextTaxYear.toItsaStatusShortTaxYear).map(_.status)
              (current, next)
            case Left(error) =>
              throw new InternalServerException(s"[MandationStatusController] - Failure response fetching mandation status. ${error.status}, ${error.reason}")
          }
        }

        statusResult flatMap {
          case (maybeCurrentYearStatus, maybeNextYearStatus) =>
            val currentYearStatus = maybeCurrentYearStatus.getOrElse(
              throw new InternalServerException("[MandationStatusController] - No itsa status found for current tax year")
            )
            val nextYearStatus = maybeNextYearStatus.getOrElse(
              throw new InternalServerException("[MandationStatusController] - No itsa status found for next tax year")
            )
            auditService.audit(MandationStatusAuditModel(
              agentReferenceNumber = getArnFromEnrolments(enrolments),
              utr = requestBody.utr,
              nino = requestBody.nino,
              currentYear = AccountingPeriodUtil.getCurrentTaxYear.toShortTaxYear,
              currentYearStatus = currentYearStatus.value,
              nextYear = AccountingPeriodUtil.getNextTaxYear.toShortTaxYear,
              nextYearStatus = nextYearStatus.value
            )) map { _ =>
              Ok(Json.toJson(MandationStatusResponse(currentYearStatus = currentYearStatus, nextYearStatus = nextYearStatus)))
            }
        }
      }
    }
  }

}
