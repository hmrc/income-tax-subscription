/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.{AuthService, SubscriptionDataService}
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.ExecutionContext

@Singleton
class SubscriptionDataController @Inject()(authService: AuthService,
                                           subscriptionDataService: SubscriptionDataService,
                                           cc: ControllerComponents)
                                         (implicit ec: ExecutionContext) extends BackendController(cc) {


  def getAllSelfEmployments: Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      subscriptionDataService.getAllSelfEmployments.map {
        case Some(data) => Ok(data)
        case None => NoContent
      }
    }
  }

  def retrieveSelfEmployments(id: String): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      subscriptionDataService.retrieveSelfEmployments(id).map {
        case Some(data) => Ok(data)
        case None => NoContent
      }
    }
  }

  def insertSelfEmployments(id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    authService.authorised() {
      subscriptionDataService.insertSelfEmployments(dataId = id, request.body).map(_ => Ok)
    }
  }

  def deleteAllSessionData: Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      subscriptionDataService.deleteSessionData.map { result =>
        if (result.ok) {
          Ok
        } else {
          throw new RuntimeException(
            "[SubscriptionDataController][deleteAllSessionData] - delete session data failed with code " + result.code.getOrElse("")
          )
        }
      }
    }
  }
}
