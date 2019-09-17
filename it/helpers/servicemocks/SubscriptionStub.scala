/*
 * Copyright 2018 HM Revenue & Customs
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

package helpers.servicemocks

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import connectors.deprecated.SubscriptionConnector._
import connectors.deprecated.SubscriptionConnector
import models.subscription.IncomeSourceModel
import models.subscription.business._
import helpers.IntegrationTestConstants._
import models.subscription.property._
import play.api.http.Status._
import play.api.libs.json.Json

object SubscriptionStub extends WireMockMethods {
  val testBusinessSubscriptionResponse: BusinessSubscriptionSuccessResponseModel =
    BusinessSubscriptionSuccessResponseModel(
      testSafeId,
      testMtditId,
      List(IncomeSourceModel(testSourceId))
    )

  val testBusinessSubscriptionFailedResponse: BusinessSubscriptionErrorResponseModel =
    BusinessSubscriptionErrorResponseModel(Some("BAD_REQUEST"), testErrorReason)

  val testPropertySubscriptionResponse: PropertySubscriptionResponseModel =
    PropertySubscriptionResponseModel(
      testSafeId,
      testMtditId,
      IncomeSourceModel(testSourceId)
    )

  val testPropertySubscriptionFailedResponse: PropertySubscriptionFailureModel =
    PropertySubscriptionFailureModel(Some("NOT_FOUND"), testErrorReason)

  def stubBusinessSubscribeSuccess(): StubMapping =
    when(method = POST, uri = SubscriptionConnector.businessSubscribeUri(testNino), body = businessSubscriptionRequestPayload)
    .thenReturn(status = OK, body = testBusinessSubscriptionResponse)

  def stubBusinessSubscribeFailure(): StubMapping =
    when(method = POST, uri = SubscriptionConnector.businessSubscribeUri(testNino), body = businessSubscriptionRequestPayload)
      .thenReturn(status = BAD_REQUEST, body = testBusinessSubscriptionFailedResponse)

  def stubPropertySubscribeSuccess(): StubMapping =
    when(method = POST, uri = SubscriptionConnector.propertySubscribeUri(testNino), body = Json.obj())
      .thenReturn(status = OK, body = testPropertySubscriptionResponse)

  def stubPropertySubscribeFailure(): StubMapping =
    when(method = POST, uri = SubscriptionConnector.propertySubscribeUri(testNino), body = Json.obj())
      .thenReturn(status = NOT_FOUND, body = testPropertySubscriptionFailedResponse)

  def verifyBusinessSubscribe(): Unit = verify(method = POST, uri = businessSubscribeUri(testNino), businessSubscriptionRequestPayload)
  def verifyPropertySubscribe(): Unit = verify(method = POST, uri = propertySubscribeUri(testNino), Json.obj())
}
