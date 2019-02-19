/*
 * Copyright 2019 HM Revenue & Customs
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

package controllers.digitalcontact

import javax.inject.{Inject, Singleton}
import common.Constants._
import models.digitalcontact.PaperlessPreferenceKey
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.AuthService
import services.digitalcontact.PaperlessPreferenceService
import uk.gov.hmrc.play.bootstrap.controller.BaseController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaperlessPreferenceController @Inject()(authService: AuthService,
                                              paperlessPreferenceService: PaperlessPreferenceService,
                                             cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends BaseController(cc) {

  import authService._

  def storeNino(token: String): Action[JsValue] = Action.async(parse.json) { implicit req =>
    authorised() {
      (req.body \ ninoJsonKey).validate[String] match {
        case JsSuccess(nino, _) =>
          val model = PaperlessPreferenceKey(token, nino)
          paperlessPreferenceService.storeNino(model) map {
            _ => Created
          }
        case JsError(errors) =>
          Future.successful(
            BadRequest(s"$errors")
          )
      }
    }
  }

  def getNino(token: String): Action[AnyContent] = Action.async { implicit req =>
    authorised() {
      paperlessPreferenceService.getNino(token).map {
        case Some(model) => Ok(Json.toJson(model)(PaperlessPreferenceKey.writes))
        case None => NotFound
      }
    }
  }
}
