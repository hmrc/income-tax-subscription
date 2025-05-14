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

import config.AppConfig
import config.featureswitch.{FeatureSwitching, GetNewITSABusinessDetails}
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.{AuthStub, BusinessDetailsStub, ITSABusinessDetailsStub}
import models.frontend.{FEFailureResponse, FESuccessResponse}
import play.api.http.Status._
import play.api.libs.json.Json

class SubscriptionStatusControllerISpec extends ComponentSpecBase with FeatureSwitching {

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(GetNewITSABusinessDetails)
  }

  "subscribe" should {
    "when the feature switch is off call the subscription service successfully when auth succeeds" in {
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

    "when the feature switch is off return UNAUTHORIZED when auth fails" in {
      Given("I setup the wiremock stubs")
      AuthStub.stubAuthFailure()

      When("I call GET /subscription/:nino where nino is the test nino")
      val res = IncomeTaxSubscription.getSubscriptionStatus(testNino)

      Then("The result should have a HTTP status of OK and a body containing the MTDID")
      res should have(
        httpStatus(UNAUTHORIZED)
      )
    }

    "when the feature switch is off return BAD_REQUEST when getBusinessDetails returns BAD_REQUEST" in {
      Given("I setup the wiremock stubs")
      AuthStub.stubAuthSuccess()
      BusinessDetailsStub.stubGetBusinessDetailsFailure()

      When("I call GET /subscription/:nino where nino is the test nino")
      val res = IncomeTaxSubscription.getSubscriptionStatus(testNino)

      Then("The result should have a HTTP status of OK and a body containing the MTDID")
      res should have(
        httpStatus(BAD_REQUEST)
      )

      Then("Get business details should have been called")
      BusinessDetailsStub.verifyGetBusinessDetails()
    }

    "when the feature switch is on and auth succeeds, return OK with mtditId" in {
      enable(GetNewITSABusinessDetails)
      AuthStub.stubAuthSuccess()
      ITSABusinessDetailsStub.stubGetITSABusinessDetailsSuccess()

      val res = IncomeTaxSubscription.getSubscriptionStatus(testNino)

      res should have(
        httpStatus(OK),
        jsonBodyAs[FESuccessResponse](FESuccessResponse(Some(testMtditId)))
      )

      ITSABusinessDetailsStub.verifyGetITSABusinessDetails()
    }

    "when the feature switch is on and auth fails, return UNAUTHORIZED" in {
      enable(GetNewITSABusinessDetails)
      AuthStub.stubAuthFailure()

      val res = IncomeTaxSubscription.getSubscriptionStatus(testNino)

      res should have(
        httpStatus(UNAUTHORIZED)
      )
    }

    "when the feature switch is on returns a BAD_REQUEST" in {
      enable(GetNewITSABusinessDetails)
      AuthStub.stubAuthSuccess()
      ITSABusinessDetailsStub.stubGetITSABusinessDetailsBadRequest()

      val res = IncomeTaxSubscription.getSubscriptionStatus(testNino)

      res should have(
        httpStatus(INTERNAL_SERVER_ERROR)
      )

      ITSABusinessDetailsStub.verifyGetITSABusinessDetails()
    }

    "when the feature switch is on returns a INTERNAL_SERVER_ERROR" in {
      enable(GetNewITSABusinessDetails)
      AuthStub.stubAuthSuccess()
      ITSABusinessDetailsStub.stubGetITSABusinessDetailsInternalServerError()

      val res = IncomeTaxSubscription.getSubscriptionStatus(testNino)

      res should have(
        httpStatus(INTERNAL_SERVER_ERROR)
      )

      ITSABusinessDetailsStub.verifyGetITSABusinessDetails()
    }

    "when the feature switch is on and connector returns NOT_FOUND" in {
      enable(GetNewITSABusinessDetails)
      AuthStub.stubAuthSuccess()
      ITSABusinessDetailsStub.stubGetITSABusinessDetailsNotFound()

      val res = IncomeTaxSubscription.getSubscriptionStatus(testNino)

      res should have(
        httpStatus(OK)
      )

      ITSABusinessDetailsStub.verifyGetITSABusinessDetails()
    }

    "when the feature switch is on and is invalid success missing mtdId" in {
      enable(GetNewITSABusinessDetails)
      AuthStub.stubAuthSuccess()
      ITSABusinessDetailsStub.stubGetITSABusinessDetailsInvalidSuccess()

      val res = IncomeTaxSubscription.getSubscriptionStatus(testNino)

      res should have(
        httpStatus(INTERNAL_SERVER_ERROR)
      )

      ITSABusinessDetailsStub.verifyGetITSABusinessDetails()
    }
}
}
