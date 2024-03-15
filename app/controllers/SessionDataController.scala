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
import play.api.Logging
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.{AuthService, SessionDataService}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class SessionDataController @Inject()(authService: AuthService,
                                      sessionDataService: SessionDataService,
                                      cc: ControllerComponents)
                                     (implicit ec: ExecutionContext) extends BackendController(cc) with Logging with Extractors {


  def getAllSessionData: Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      sessionDataService.getAllSessionData.map {
        case Some(data) => Ok(data)
        case None => NoContent
      }
    }
  }

  def retrieveSessionData(id: String): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      sessionDataService.getSessionData(id).map {
        case Some(data) => Ok(data)
        case None => NoContent
      }
    }
  }

  def insertSessionData(id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    authService.authorised() {
      sessionDataService.insertSessionData(
        dataId = id,
        request.body
      ).map(_ => Ok)
    }
  }

  def deleteSessionData(id: String): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      sessionDataService.deleteSessionData(
        dataId = id
      ).map(_ => Ok)
    }
  }
}
