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

package controllers.registeration

import javax.inject.Inject

import play.api.Application
import play.api.mvc.{Action, AnyContent}
import services.RegistrationService
import uk.gov.hmrc.play.microservice.controller.BaseController
import scala.concurrent.ExecutionContext.Implicits.global

class RegistrationController @Inject()(application: Application,
                                       registrationService: RegistrationService) extends BaseController {

  def register(nino: String, firstName: String, lastName: String, isAgent: Boolean): Action[AnyContent] = Action.async {
    implicit request =>
      val response = registrationService.register(isAgent, nino, firstName, lastName)
      response map {
        //TODO
        case Right(r) => Ok(r.safeId)
        case Left(l) => Ok("")
      }
  }

}