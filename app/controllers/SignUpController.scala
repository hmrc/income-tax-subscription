/*
 * Copyright 2020 HM Revenue & Customs
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

import connectors.SignUpConnector
import javax.inject.{Inject, Singleton}
import play.api.Logger.logger
import play.api.libs.json.Json
import play.api.mvc._
import services.AuthService
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.ExecutionContext

@Singleton
class SignUpController @Inject()(authService: AuthService,
                                 signUpConnector: SignUpConnector,
                                 cc: ControllerComponents)(implicit ec: ExecutionContext) extends BackendController(cc) {

  def signUp(nino: String): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      signUpConnector.signUp(nino).map {
        case Right(response) => Ok(Json.toJson(response))
        case Left(error) => logger.error(s"Error processing Sign up request with status ${error.status} and message ${error.reason}")
          InternalServerError("Failed Sign up")
      }

    }
  }

}