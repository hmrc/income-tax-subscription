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

package connectors.mocks

import audit.Logging
import config.AppConfig
import connectors.{BusinessConnectorUtil, PropertyConnectorUtil, SubscriptionConnector}
import models.subscription.business.BusinessSubscriptionRequestModel
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status._
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.{HeaderCarrier, HttpPost}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import utils.JsonUtils._
import utils.TestConstants.{BusinessSubscriptionResponse, PropertySubscriptionResponse, _}

import scala.concurrent.ExecutionContext

trait MockSubscriptionConnector extends MockitoSugar {
  val mockSubscriptionConnector = mock[SubscriptionConnector]

  def mockBusinessSubscribe(nino: String, request: BusinessSubscriptionRequestModel)(response: BusinessConnectorUtil.Response): Unit = {
    when(mockSubscriptionConnector.businessSubscribe(
      ArgumentMatchers.eq(nino),
      ArgumentMatchers.eq(request),
      ArgumentMatchers.any()
    )(
      ArgumentMatchers.any[HeaderCarrier],
      ArgumentMatchers.any[ExecutionContext]
    ))
      .thenReturn(response)
  }

  def mockPropertySubscribe(nino: String)(response: PropertyConnectorUtil.Response): Unit = {
    when(mockSubscriptionConnector.propertySubscribe(
      ArgumentMatchers.eq(nino),
      ArgumentMatchers.any()
    )(
      ArgumentMatchers.any[HeaderCarrier],
      ArgumentMatchers.any[ExecutionContext]
    ))
      .thenReturn(response)
  }
}

trait TestSubscriptionConnector extends MockHttp with GuiceOneAppPerSuite {

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val httpClient: HttpClient = mockHttpClient
  lazy val logging: Logging = app.injector.instanceOf[Logging]

  val mockPropertySubscribe = (setupMockPropertySubscribe(testNino) _).tupled

  def mockBusinessSubscribe(payload: BusinessSubscriptionRequestModel) = (setupMockBusinessSubscribe(testNino, payload) _).tupled

  val propertySubscribeSuccess = (OK, PropertySubscriptionResponse.successResponse(testSafeId, testMtditId, testSourceId))
  val businessSubscribeSuccess = (OK, BusinessSubscriptionResponse.successResponse(testSafeId, testMtditId, testSourceId))

  object TestSubscriptionConnector extends SubscriptionConnector(appConfig, httpClient, logging)

  def setupMockBusinessSubscribe(nino: String, payload: BusinessSubscriptionRequestModel)(status: Int, response: JsValue): Unit =
    setupMockHttpPost(url = TestSubscriptionConnector.businessSubscribeUrl(nino), payload)(status, response)

  def setupMockPropertySubscribe(nino: String)(status: Int, response: JsValue): Unit =
    setupMockHttpPost(url = TestSubscriptionConnector.propertySubscribeUrl(nino), optionUtl("{}": JsValue))(status, response)

}
