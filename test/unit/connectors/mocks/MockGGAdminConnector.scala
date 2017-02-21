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
import config.AppConfig
import connectors.GGAdminConnector
import models.gg.KnownFactsRequest
import org.scalatestplus.play.OneAppPerSuite
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.JsValue
import uk.gov.hmrc.play.http.{HttpGet, HttpPost}
import utils.Implicits._
import utils.TestConstants.GG.KnownFactsResponse

trait MockGGAdminConnector extends MockHttp with OneAppPerSuite {

  lazy val config: Configuration = app.injector.instanceOf[Configuration]
  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val logging: Logging = app.injector.instanceOf[Logging]
  lazy val httpPost: HttpPost = mockHttpPost
  lazy val httpGet: HttpGet = mockHttpGet

  def mockAddKnownFacts(request: KnownFactsRequest) = (setupAddKnownFacts(request) _).tupled

  object TestGGAdminConnector extends GGAdminConnector(config, appConfig, logging, httpPost)

  lazy val addKnownFactsSuccess = (OK, KnownFactsResponse.successResponse(1))

  def setupAddKnownFacts(request: KnownFactsRequest)(status: Int, response: JsValue): Unit =
    setupMockHttpPost(url = TestGGAdminConnector.addKnownFactsUrl, request)(status, response)

}
