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

package controllers.subscription

import connectors.RegistrationConnector.newRegistrationUri
import connectors.SubscriptionConnector.businessSubscribeUri
import connectors._
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.WireMockDSL.HTTPVerbMapping.{Get, Post}
import helpers.WireMockDSL._
import helpers.servicemocks._
import helpers.servicemocks.BusinessDetailsStub._
import helpers.servicemocks.GGAdminStub._
import models.frontend.FESuccessResponse
import play.api.http.Status._
import play.api.libs.json.Json

class SubscriptionControllerISpec extends ComponentSpecBase {
  "subscribe" should {
    "call the subscription service successfully when auth succeeds for a business registration" in {
      Given("I setup the wiremock stubs")
      AuthStub.stubAuthSuccess()
      RegistrationStub.stubNewRegistrationSuccess()
      SubscriptionStub.stubBusinessSubscribeSuccess()
      GGAdminStub.stubAddKnownFactsSuccess()
      GGConnectorStub.stubEnrolSuccess()
      AuthenticatorStub.stubRefreshProfileSuccess()

      When("I call /subscription/:nino where nino is the test nino")
      val res = IncomeTaxSubscription.createSubscription(feBusinessRequest)

      Then("The result should have a HTTP status of OK and a body containing the MTDID")
      res should have(
        httpStatus(OK),
        jsonBodyAs[FESuccessResponse](FESuccessResponse(Some(testMtditId)))
      )
    }
//    "call the subscription service successfully when auth succeeds for a property registration" in {
//      when(method = GET, uri = authority)
//        .thenReturn(status = OK, body = successfulAuthResponse)
//      when(method = GET, uri = authIDs)
//        .thenReturn(status = OK, body = userIDs)
//      when(method = POST, uri = newRegistrationUri(testNino), body = registerRequestPayload)
//        .thenReturn(status = OK, body = registrationResponse)
//      when(method = POST, uri = SubscriptionConnector.propertySubscribeUri(testNino), body = Json.obj())
//        .thenReturn(status = OK, body = testPropertySubscriptionResponse)
//      when(method = POST, uri = GGAdminConnector.addKnownFactsUri)
//        .thenReturn(status = OK, body = testAddKnownFactsResponse)
//      when(method = POST, uri = GGConnector.enrolUri)
//        .thenReturn(status = OK)
//      when(method = POST, uri = AuthenticatorConnector.refreshProfileUri)
//        .thenReturn(status = NO_CONTENT)
//
//      IncomeTaxSubscription.createSubscription(fePropertyRequest) should have(
//        httpStatus(OK),
//        jsonBodyAs[FESuccessResponse](FESuccessResponse(Some(testMtditId)))
//      )
//    }
//
//    "call the subscription service successfully when auth succeeds for a business and property registration" in {
//      stub when Get(authority) thenReturn successfulAuthResponse
//      stub when Get(authIDs) thenReturn userIDs
//      multiline(
//        stub
//          when Post(newRegistrationUri(testNino), registerRequestPayload)
//          thenReturn registrationResponse
//      )
//      multiline(
//        stub
//          when Post(SubscriptionConnector.propertySubscribeUri(testNino), Json.obj())
//          thenReturn testPropertySubscriptionResponse
//      )
//      multiline(
//        stub
//          when Post(businessSubscribeUri(testNino), businessSubscriptionRequestPayload)
//          thenReturn testBusinessSubscriptionResponse
//      )
//      stub when Post(GGAdminConnector.addKnownFactsUri) thenReturn testAddKnownFactsResponse
//      stub when Post(GGConnector.enrolUri) thenReturn OK
//      stub when Post(AuthenticatorConnector.refreshProfileUri) thenReturn NO_CONTENT
//
//      IncomeTaxSubscription.createSubscription(feBothRequest) should have(
//        httpStatus(OK),
//        jsonBodyAs[FESuccessResponse](FESuccessResponse(Some(testMtditId)))
//      )
//
//      stub verify Post(businessSubscribeUri(testNino), businessSubscriptionRequestPayload)
//      stub verify Post(SubscriptionConnector.propertySubscribeUri(testNino), Json.obj())
//    }
//
//    "fail when get authority fails" in {
//      stubGetAuthorityFailure()
//
//      IncomeTaxSubscription.createSubscription(feBusinessRequest) should have(
//        httpStatus(UNAUTHORIZED),
//        emptyBody
//      )
//    }
  }
}
