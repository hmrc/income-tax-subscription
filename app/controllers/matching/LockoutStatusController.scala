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

package controllers.matching

import models.lockout.LockoutRequest
import models.matching.LockoutResponse
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.{AuthService, LockoutStatusService}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LockoutStatusController @Inject()(authService: AuthService,
                                        lockoutStatusService: LockoutStatusService,
                                        cc: ControllerComponents)
                                       (implicit ec: ExecutionContext) extends BackendController(cc) {


  def checkLockoutStatus(arn: String): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      lockoutStatusService.checkLockoutStatus(arn).map {
        case Right(Some(lock)) => Ok(Json.toJson(lock)(LockoutResponse.feWritter))
        case Right(None) => NotFound
        case Left(_) => InternalServerError
      }
    }
  }

  def lockoutAgent(arn: String): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      request.body.asJson.map(Json.fromJson[LockoutRequest](_).asOpt) match {
        case Some(Some(req)) =>
          lockoutStatusService.lockoutAgent(arn, req).map {
            case Right(Some(lock)) => Created(Json.toJson(lock)(LockoutResponse.feWritter))
            case _ => InternalServerError
          }
        case _ =>
          Future.successful(BadRequest)
      }

    }
  }

}

