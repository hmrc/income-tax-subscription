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

package unit.connectors.mocks

import audit.Logging
import connectors.{BusinessSubscriptionConnector, RegistrationConnector}
import models.subscription.business.BusinessSubscriptionRequestModel
import org.scalatestplus.play.OneAppPerSuite
import play.api.Configuration
import play.api.libs.json.JsValue
import uk.gov.hmrc.play.http.{HttpGet, HttpPost}
import utils.Implicits._

trait MockBusinessSubscriptionConnector extends MockHttp with OneAppPerSuite {

  lazy val config: Configuration = app.injector.instanceOf[Configuration]
  lazy val logging: Logging = app.injector.instanceOf[Logging]
  lazy val httpPost: HttpPost = mockHttpPost
  lazy val httpGet: HttpGet = mockHttpGet

  object TestBusinessSubsscriptionConnector extends BusinessSubscriptionConnector(config, logging, httpPost, httpGet)

  def setupMockBusinessSubscribe(nino: String, payload: BusinessSubscriptionRequestModel)(status: Int, response: JsValue): Unit =
    setupMockHttpPost(url = TestBusinessSubsscriptionConnector.businessSubscribeUrl(nino), payload)(status, response)

}
