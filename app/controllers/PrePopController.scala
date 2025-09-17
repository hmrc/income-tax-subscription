/*
 * Copyright 2024 HM Revenue & Customs
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

import common.Constants.hmrcAsAgent
import config.AppConfig
import config.featureswitch.FeatureSwitching
import connectors.hip.HipPrePopConnector
import models.{PrePopAuditModel, PrePopData}
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.AuthService
import services.monitoring.AuditService
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.allEnrolments
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class PrePopController @Inject()(authService: AuthService,
                                 auditService: AuditService,
                                 hipPrePopConnector: HipPrePopConnector,
                                 val appConfig: AppConfig,
                                 cc: ControllerComponents)(implicit ec: ExecutionContext) extends BackendController(cc) with Logging with FeatureSwitching {

  def prePop(nino: String): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised().retrieve(allEnrolments) { allEnrolments =>
      hipPrePopConnector.getHipPrePopData(nino).map {
        case Right(value) =>
          val prePopData: PrePopData = PrePopData(
            selfEmployment = if (value.selfEmp.isEmpty) None else Some(value.toPrePopSelfEmployment)
          )
          auditService.extendedAudit(PrePopAuditModel(
            prePopData = prePopData,
            nino = nino,
            maybeArn = allEnrolments.getEnrolment(hmrcAsAgent).flatMap(_.identifiers.headOption).map(_.value)
          ))
          Ok(Json.toJson(prePopData))
        case Left(error) =>
          logger.error(s"[PrePopController][prePop] - Error when fetching pre-pop data. Status: ${error.status}, Reason: ${error.reason}")
          InternalServerError
      }
    }
  }

}
