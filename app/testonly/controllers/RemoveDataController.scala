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

package testonly.controllers

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import repositories.SubscriptionDataRepository
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RemoveDataController @Inject()(subscriptionDataRepository: SubscriptionDataRepository,
                                     cc: ControllerComponents)
                                    (implicit ec: ExecutionContext) extends BackendController(cc) {

  def removeData(utr: String): Action[AnyContent] = Action.async { _ =>
    subscriptionDataRepository.find(Json.obj("utr" -> utr), None) flatMap { found =>
      Future.sequence(
        found
          .map(value => (value \ "reference").as[String])
          .map(subscriptionDataRepository.deleteDataFromReference)
      )
    } map { _ =>
      Ok
    }
  }

}
