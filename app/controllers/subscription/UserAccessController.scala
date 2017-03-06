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

package controllers.subscription

import javax.inject.Inject

import auth.{Authenticated, LoggedIn, NotLoggedIn}
import connectors.AuthConnector
import models.throttling.{CanAccess, LimitReached}
import play.api.mvc.Action
import services.{MetricsService, UserAccessService}
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserAccessControllerImp @Inject()(metrics: MetricsService, userAccessServ: UserAccessService) extends UserAccessController {
  override val auth = AuthConnector
  val userAccessService = userAccessServ
  override val metricsService: MetricsService = metrics
}

trait UserAccessController extends BaseController with Authenticated {

  val userAccessService: UserAccessService
  val metricsService: MetricsService

  def checkUserAccess(nino: String) = Action.async {
    implicit request =>
      val timer = metricsService.userAccessCRTimer.time()
      authenticated {
        case NotLoggedIn => Future.successful(Forbidden)
        case LoggedIn(context) =>
          userAccessService.checkUserAccess(context.ids.internalId) flatMap {
            case CanAccess =>
              timer.stop()
              Future.successful(Ok)
            case LimitReached =>
              timer.stop()
              Future.successful(TooManyRequests)
          }
      }
  }
}
