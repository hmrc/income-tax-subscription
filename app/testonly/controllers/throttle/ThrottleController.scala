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

package testonly.controllers.throttle

import play.api.i18n.MessagesApi
import play.api.mvc.ControllerComponents
import repositories.ThrottlingRepository
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class ThrottleController @Inject()(
                                    override val messagesApi: MessagesApi,
                                    cc: ControllerComponents,
                                    throttlingRepository: ThrottlingRepository,
                                    val servicesConfig: ServicesConfig
                                  )(
                                    implicit ec: ExecutionContext)
  extends BackendController(cc) {

  lazy val get = Action.async { implicit req =>
    req.getQueryString("throttle") match {
      case Some(throttleId) =>
        val throttleKey = getThrottleKey(throttleId)
        val configValueMaybe: Option[Int] = Try {
          servicesConfig.getInt(throttleKey)
        }.toOption
        val propsValueMaybe = sys.props.get(throttleKey).map(v => v.toInt)
        val throttleValMaybe: Option[Int] = propsValueMaybe orElse configValueMaybe
        throttleValMaybe match {
          case Some(throttleVal) =>
            throttlingRepository.stateOfThrottle(throttleId)
              .map(currentValues => Ok(s"$throttleKey is set to ${currentValues._1} out of $throttleVal (at ${currentValues._2})"))
          case _ => Future.successful(BadRequest(s"Throttle id $throttleId has no configured value"))
        }
      case _ => Future.successful(BadRequest(s"No throttle id provided"))
    }
  }

  private def getThrottleKey(throttleId: String) = {
    s"throttle.$throttleId.max"
  }

  lazy val update = Action.async { implicit req =>
    val throttleIdMaybe = req.getQueryString("throttle")
    val newThrottleValMaybe = req.getQueryString("value")
    Future.successful(
      (throttleIdMaybe, newThrottleValMaybe) match {
        case (Some(throttleId), Some(newThrottleVal)) =>
          val throttleKey = getThrottleKey(throttleId)
          sys.props += (throttleKey -> newThrottleVal)
          Ok(throttleKey + " has been set to " + newThrottleVal)
        case _ => BadRequest(s"Throttle id given as $throttleIdMaybe, throttleVal given as $newThrottleValMaybe")
      }
    )
  }
}
