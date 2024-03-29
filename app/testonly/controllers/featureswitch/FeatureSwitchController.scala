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

package testonly.controllers.featureswitch

import config.AppConfig
import config.featureswitch.FeatureSwitch._
import config.featureswitch.{FeatureSwitch, FeatureSwitching}
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.mvc.{ControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.Future

class FeatureSwitchController @Inject()(override val messagesApi: MessagesApi,
                                        cc: ControllerComponents,
                                        val appConfig: AppConfig
                                       )
  extends BackendController(cc) with FeatureSwitching {

  private def returnCurrentSettings = Ok(Json.toJson(switches.map(fs => FeatureSwitchSetting(fs, isEnabled(fs)))))

  lazy val get = Action.async { _ => Future.successful(returnCurrentSettings) }

  lazy val update = Action.async { implicit req =>
    Future.successful(
      Json.fromJson[List[FeatureSwitchSetting]](req.body.asJson.get).asOpt.fold[Result](BadRequest) { settingRequests =>
        settingRequests.foreach { setting =>
          val fs = FeatureSwitch(setting)
          if (setting.enable) enable(fs) else disable(fs)
        }
        returnCurrentSettings
      })
  }

}
