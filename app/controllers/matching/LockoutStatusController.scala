/*
 * Copyright 2017 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}

import models.matching.LockoutResponse.format
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import services.{AuthService, LockoutStatusService}
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global
@Singleton
class LockoutStatusController @Inject()(authService: AuthService,
                                        lockoutStatusService: LockoutStatusService) extends BaseController {

  import authService._

  def checkLockoutStatus(arn: String): Action[AnyContent] = Action.async { implicit request =>
//    authorised() {
      lockoutStatusService.checkLockoutStatus(arn).map {
        case Right(Some(arn)) => Ok(Json.toJson(arn))
        case Right(None) => NotFound
        case Left(_) => InternalServerError
//      }
    }
  }

}

//object LockoutStatusController {}