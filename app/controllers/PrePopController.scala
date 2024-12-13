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
import connectors.PrePopConnector
import models.PrePopAuditModel
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.AuthService
import services.monitoring.AuditService
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.allEnrolments
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PrePopController @Inject()(authService: AuthService,
                                 auditService: AuditService,
                                 prePopConnector: PrePopConnector,
                                 cc: ControllerComponents)(implicit ec: ExecutionContext) extends BackendController(cc) with Logging {

  def prePop(nino: String): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised().retrieve(allEnrolments) { allEnrolments =>
      prePopConnector.getPrePopData(nino) flatMap {
        case Right(value) =>
          val agentReferenceNumber: Option[String] = allEnrolments.getEnrolment(hmrcAsAgent).flatMap(_.identifiers.headOption).map(_.value)
          auditService.extendedAudit(PrePopAuditModel(prePopData = value, nino = nino, maybeArn = agentReferenceNumber)) map { _ =>
            Ok(Json.toJson(value))
          }
        case Left(error) =>
          logger.error(s"[PrePopController][prePop] - Error when fetching pre-pop data. Status: ${error.status}, Reason: ${error.reason}")
          Future.successful(InternalServerError)
      }
    }
  }


}