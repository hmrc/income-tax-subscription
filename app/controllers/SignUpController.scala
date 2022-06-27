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

import common.Extractors
import config.AppConfig
import connectors.SignUpConnector
import models.monitoring.{RegistrationFailureAudit, RegistrationSuccessAudit}
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._
import services.AuthService
import services.monitoring.AuditService
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class SignUpController @Inject()(authService: AuthService, auditService: AuditService, signUpConnector: SignUpConnector,
                                 cc: ControllerComponents, appConfig: AppConfig)(implicit ec: ExecutionContext) extends BackendController(cc) with Extractors {

  val logger: Logger = Logger(this.getClass)

  def signUp(nino: String): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised().retrieve(Retrievals.allEnrolments) { enrolments =>
      signUpConnector.signUp(nino).map {
        case Right(response) => {
          val path: Option[String] = request.headers.get(ITSASessionKeys.RequestURI)
          auditService.audit(RegistrationSuccessAudit(getArnFromEnrolments(enrolments), nino, response.mtdbsa, appConfig.desAuthorisationToken, path))
          Ok(Json.toJson(response))
        }
        case Left(error) => logger.error(s"Error processing Sign up request with status ${error.status} and message ${error.reason}")
          auditService.audit(RegistrationFailureAudit(nino, error.status, error.reason))
          InternalServerError("Failed Sign up")
      }

    }
  }
}
