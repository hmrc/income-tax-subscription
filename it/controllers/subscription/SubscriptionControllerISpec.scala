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
import helpers.servicemocks.BusinessSubscriptionStub.stubBusinessIncomeSubscription
import helpers.servicemocks.PropertySubscriptionStub.stubPropertyIncomeSubscription
import helpers.servicemocks.{AuthStub, RegistrationStub}
import play.api.http.Status._
import play.api.libs.json.Json
import services.SubmissionOrchestrationService.SuccessfulSubmission


class SubscriptionControllerISpec extends ComponentSpecBase {
  "subscribe" should {
    "call the subscription service successfully when auth succeeds for a business registration" in {
      AuthStub.stubAuthSuccess()
      RegistrationStub.stubRegistration(testNino, isAnAgent = false)(OK, "")
      stubBusinessIncomeSubscription(testNino, testBusinessIncomeModel)(OK, Json.obj("mtditId" -> testMtditId))

      val res = IncomeTaxSubscription.createSubscriptionRefactor(incomeSourceBusiness)

      res should have(
        httpStatus(OK),
        jsonBodyAs[SuccessfulSubmission](SuccessfulSubmission(testMtditId))
      )
    }

    "call the subscription service successfully when auth succeeds for a property registration" in {
      AuthStub.stubAuthSuccess()
      RegistrationStub.stubRegistration(testNino, isAnAgent = false)(OK, "")
      stubPropertyIncomeSubscription(testNino, testPropertyIncomeModel)(OK, Json.obj("mtditId" -> testMtditId))

      val res = IncomeTaxSubscription.createSubscriptionRefactor(incomeSourceProperty)

      res should have(
        httpStatus(OK),
        jsonBodyAs[SuccessfulSubmission](SuccessfulSubmission(testMtditId))
      )
    }

    "call the subscription service successfully when auth succeeds for a business and property registration" in {
      AuthStub.stubAuthSuccess()
      RegistrationStub.stubRegistration(testNino, isAnAgent = false)(OK, "")
      stubBusinessIncomeSubscription(testNino, testBusinessIncomeModel)(OK, Json.obj("mtditId" -> testMtditId))
      stubPropertyIncomeSubscription(testNino, testPropertyIncomeModel)(OK, Json.obj("mtditId" -> testMtditId))

      val res = IncomeTaxSubscription.createSubscriptionRefactor(incomeSourceBoth)

      res should have(
        httpStatus(OK),
        jsonBodyAs[SuccessfulSubmission](SuccessfulSubmission(testMtditId))
      )
    }

    "fail when Auth returns an UNAUTHORIZED response" in {
      AuthStub.stubAuthFailure()

      val res = IncomeTaxSubscription.createSubscriptionRefactor(incomeSourceBusiness)

      res should have(
        httpStatus(UNAUTHORIZED)
      )
    }

    "fail when Registration returns a BAD_REQUEST response" in {
      AuthStub.stubAuthSuccess()
      RegistrationStub.stubRegistration(testNino, isAnAgent = false)(BAD_REQUEST, RegistrationStub.failedRegistrationResponse)

      val res = IncomeTaxSubscription.createSubscriptionRefactor(incomeSourceBusiness)

      res should have(
        httpStatus(BAD_REQUEST)
      )
    }

    "fail when Business Subscription returns a BAD_REQUEST response" in {
      AuthStub.stubAuthSuccess()
      RegistrationStub.stubRegistration(testNino, isAnAgent = false)(OK, RegistrationStub.successfulRegistrationResponse)
      stubBusinessIncomeSubscription(testNino, testBusinessIncomeModel)(BAD_REQUEST, Json.obj())

      val res = IncomeTaxSubscription.createSubscriptionRefactor(incomeSourceBusiness)

      res should have(
        httpStatus(INTERNAL_SERVER_ERROR)
      )
    }

    "fail when Property Subscription returns a BAD_REQUEST response" in {
      AuthStub.stubAuthSuccess()
      RegistrationStub.stubRegistration(testNino, isAnAgent = false)(OK, RegistrationStub.successfulRegistrationResponse)
      stubPropertyIncomeSubscription(testNino, testPropertyIncomeModel)(BAD_REQUEST, Json.obj())

      val res = IncomeTaxSubscription.createSubscriptionRefactor(incomeSourceProperty)

      res should have(
        httpStatus(INTERNAL_SERVER_ERROR)
      )
    }

    "fail when BOTH Subscriptions returns BAD_REQUEST responses during a dual Subscription" in {
      AuthStub.stubAuthSuccess()
      RegistrationStub.stubRegistration(testNino, isAnAgent = false)(OK, RegistrationStub.successfulRegistrationResponse)
      stubBusinessIncomeSubscription(testNino, testBusinessIncomeModel)(BAD_REQUEST, Json.obj())
      stubPropertyIncomeSubscription(testNino, testPropertyIncomeModel)(BAD_REQUEST, Json.obj())

      val res = IncomeTaxSubscription.createSubscriptionRefactor(incomeSourceBoth)

      res should have(
        httpStatus(INTERNAL_SERVER_ERROR)
      )
    }

    "fail when Business Subscription returns BAD_REQUEST but Property Subscription has no errors during a BOTH Subscription" in {
      AuthStub.stubAuthSuccess()
      RegistrationStub.stubRegistration(testNino, isAnAgent = false)(OK, RegistrationStub.successfulRegistrationResponse)
      stubBusinessIncomeSubscription(testNino, testBusinessIncomeModel)(BAD_REQUEST, Json.obj())
      stubPropertyIncomeSubscription(testNino, testPropertyIncomeModel)(OK, Json.obj("mtditId" -> testMtditId))

      val res = IncomeTaxSubscription.createSubscriptionRefactor(incomeSourceBoth)

      res should have(
        httpStatus(INTERNAL_SERVER_ERROR)
      )
    }

    "fail when Property Subscription returns BAD_REQUEST but Business Subscription has no errors during a BOTH Subscription" in {
      AuthStub.stubAuthSuccess()
      RegistrationStub.stubRegistration(testNino, isAnAgent = false)(OK, RegistrationStub.successfulRegistrationResponse)
      stubBusinessIncomeSubscription(testNino, testBusinessIncomeModel)(OK, Json.obj("mtditId" -> testMtditId))
      stubPropertyIncomeSubscription(testNino, testPropertyIncomeModel)(BAD_REQUEST, Json.obj())

      val res = IncomeTaxSubscription.createSubscriptionRefactor(incomeSourceBoth)

      res should have(
        httpStatus(INTERNAL_SERVER_ERROR)
      )
    }
  }
}
