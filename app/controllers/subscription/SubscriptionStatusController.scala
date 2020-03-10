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

package controllers.subscription

import javax.inject.Inject
import models.frontend.FEFailureResponse
import play.api.Logger
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.{AuthService, SubscriptionStatusService}
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import utils.JsonUtils.toJsValue

import scala.concurrent.ExecutionContext.Implicits.global

class SubscriptionStatusController @Inject()(authService: AuthService,
                                             subscriptionStatusService: SubscriptionStatusService,
                                             cc: ControllerComponents) extends BackendController(cc) {

  def checkSubscriptionStatus(nino: String): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      subscriptionStatusService.checkMtditsaSubscription(nino).map {
        case Right(success) =>
          Logger.debug(s"SubscriptionStatusController.checkSubscriptionStatus - successful, responding with\n$success")
          Ok(toJsValue(success))
        case Left(failure) =>
          Logger.warn(s"SubscriptionStatusController.checkSubscriptionStatus - failed, responding with\nstatus=${failure.status}\nreason=${failure.reason}")
          Status(failure.status)(toJsValue(FEFailureResponse(failure.reason)))
      }
    }
  }
}
