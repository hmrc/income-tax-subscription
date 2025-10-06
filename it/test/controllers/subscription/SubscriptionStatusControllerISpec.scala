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
import config.featureswitch.FeatureSwitching
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.{AuthStub, BusinessDetailsStub, ITSABusinessDetailsStub}
import models.frontend.FESuccessResponse
import play.api.http.Status._

class SubscriptionStatusControllerISpec extends ComponentSpecBase with FeatureSwitching {

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  

  "subscribe" should {
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

    "when auth succeeds, return OK with mtditId" in {
      
      AuthStub.stubAuthSuccess()
      ITSABusinessDetailsStub.stubGetITSABusinessDetailsSuccess()

      val res = IncomeTaxSubscription.getSubscriptionStatus(testNino)

      res should have(
        httpStatus(OK),
        jsonBodyAs[FESuccessResponse](FESuccessResponse(Some(testMtditId)))
      )

      ITSABusinessDetailsStub.verifyGetITSABusinessDetails()
    }

    "when auth fails, return UNAUTHORIZED" in {
      
      AuthStub.stubAuthFailure()

      val res = IncomeTaxSubscription.getSubscriptionStatus(testNino)

      res should have(
        httpStatus(UNAUTHORIZED)
      )
    }

    "returns a BAD_REQUEST when appropriate" in {
      
      AuthStub.stubAuthSuccess()
      ITSABusinessDetailsStub.stubGetITSABusinessDetailsBadRequest()

      val res = IncomeTaxSubscription.getSubscriptionStatus(testNino)

      res should have(
        httpStatus(INTERNAL_SERVER_ERROR)
      )

      ITSABusinessDetailsStub.verifyGetITSABusinessDetails()
    }

    "returns a INTERNAL_SERVER_ERROR when appropriate" in {
      
      AuthStub.stubAuthSuccess()
      ITSABusinessDetailsStub.stubGetITSABusinessDetailsInternalServerError()

      val res = IncomeTaxSubscription.getSubscriptionStatus(testNino)

      res should have(
        httpStatus(INTERNAL_SERVER_ERROR)
      )

      ITSABusinessDetailsStub.verifyGetITSABusinessDetails()
    }

    "return an ok when the connector returns NOT_FOUND" in {
      AuthStub.stubAuthSuccess()
      ITSABusinessDetailsStub.stubGetITSABusinessDetailsNotFound()

      val res = IncomeTaxSubscription.getSubscriptionStatus(testNino)

      res should have(
        httpStatus(OK)
      )

      ITSABusinessDetailsStub.verifyGetITSABusinessDetails()
    }

    "return INTERNAL_SERVER_ERROR when invalid success missing mtdId" in {
      
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
