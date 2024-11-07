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

import connectors.PrePopConnector
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.AuthService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class PrePopController @Inject()(authService: AuthService,
                                 prePopConnector: PrePopConnector,
                                 cc: ControllerComponents)(implicit ec: ExecutionContext) extends BackendController(cc) with Logging {

  def prePop(nino: String): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      prePopConnector.getPrePopData(nino) map {
        case Right(value) => Ok(Json.toJson(value))
        case Left(error) =>
          logger.error(s"[PrePopController][prePop] - Error when fetching pre-pop data. Status: ${error.status}, Reason: ${error.reason}")
          InternalServerError
      }
    }
  }

}