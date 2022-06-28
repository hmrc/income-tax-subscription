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

package controllers.throttle

import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import repositories.ThrottlingRepository
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class ThrottlingController @Inject()(throttlingRepository: ThrottlingRepository,
                                     servicesConfig: ServicesConfig,
                                     cc: ControllerComponents
                                  ) extends BackendController(cc) with Logging {

  def throttled(throttleId: String): Action[AnyContent] = Action.async { _ =>
    val throttleKey = s"throttle.$throttleId.max"
    Try {
      servicesConfig.getInt(throttleKey)
    }.toOption match {
      case None =>
        logger.warn(s"No throttle max found for $throttleKey in config")
        Future.successful(BadRequest)
      case Some(max) =>
        throttlingRepository.checkThrottle(throttleId).map {
          int: Int => {
            val body = Json.toJson(int)
            if (int <= max)
              Ok(body)
            else {
              logger.info(s"Throttle max exceeded for $throttleId")
              ServiceUnavailable(body)
            }
          }
        }
    }
  }

}
