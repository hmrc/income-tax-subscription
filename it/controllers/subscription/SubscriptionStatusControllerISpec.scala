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

package controllers.subscription

import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.{AuthStub, BusinessDetailsStub}
import models.frontend.FESuccessResponse
import play.api.http.Status._
import play.api.libs.json.Json

class SubscriptionStatusControllerISpec extends ComponentSpecBase {
  "subscribe" should {
    "call the subscription service successfully when auth succeeds" in {
      Given("I setup the wiremock stubs")
      AuthStub.stubAuthSuccess()
      BusinessDetailsStub.stubGetBusinessDetailsSuccess()

      When("I call GET /subscription/:nino where nino is the test nino")
      val res = IncomeTaxSubscription.getSubscriptionStatus(testNino)

      Then("The result should have a HTTP status of OK and a body containing the MTDID")
      res should have(
        httpStatus(OK),
        jsonBodyAs[FESuccessResponse](FESuccessResponse(Some(testMtditId)))
      )

      Then("Get business details should have been called")
      BusinessDetailsStub.verifyGetBusinessDetails()
    }

    "return UNAUTHORIZED when auth fails" in {
      Given("I setup the wiremock stubs")
      AuthStub.stubAuthFailure()

      When("I call GET /subscription/:nino where nino is the test nino")
      val res = IncomeTaxSubscription.getSubscriptionStatus(testNino)

      Then("The result should have a HTTP status of OK and a body containing the MTDID")
      res should have(
        httpStatus(UNAUTHORIZED)
      )
    }

    "return BAD_REQUEST when getBusinessDetails returns BAD_REQUEST" in {
      Given("I setup the wiremock stubs")
      AuthStub.stubAuthSuccess()
      BusinessDetailsStub.stubGetBusinessDetailsFailure()

      When("I call GET /subscription/:nino where nino is the test nino")
      val res = IncomeTaxSubscription.getSubscriptionStatus(testNino)

      Then("The result should have a HTTP status of OK and a body containing the MTDID")
      res should have(
        httpStatus(BAD_REQUEST),
        jsonBodyAs(Json.obj("reason" -> BusinessDetailsStub.errorReason))
      )

      Then("Get business details should have been called")
      BusinessDetailsStub.verifyGetBusinessDetails()
    }
  }
}
