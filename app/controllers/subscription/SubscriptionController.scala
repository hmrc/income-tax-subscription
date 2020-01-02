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

import javax.inject.{Inject, Singleton}
import models.subscription.incomesource.SignUpRequest
import play.api.libs.json._
import play.api.mvc._
import services.{AuthService, SubmissionOrchestrationService}
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import scala.concurrent.ExecutionContext

@Singleton
class SubscriptionController @Inject()(submissionOrchestrationService: SubmissionOrchestrationService,
                                       authService: AuthService,
                                       cc: ControllerComponents
                                      )(implicit ec: ExecutionContext) extends BackendController(cc) {

  import authService._

  def subscribe(nino: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    authorised() {
      withJsonBody[SignUpRequest] {
        signUpRequest =>
          submissionOrchestrationService.submit(signUpRequest).map {
            response =>
              Ok(Json.toJson(response))
          }
      }
    }
  }
}
