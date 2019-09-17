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

package controllers.deprecated

import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks._
import models.frontend._
import play.api.http.Status._

class SubscriptionControllerISpec extends ComponentSpecBase {
  "subscribe" should {
    "call the subscription service successfully when auth succeeds for a business registration" in {
      Given("I setup the wiremock stubs")
      AuthStub.stubAuthSuccess()
      RegistrationStub.stubNewRegistrationSuccess()
      SubscriptionStub.stubBusinessSubscribeSuccess()

      When("I call POST /subscription/:nino where nino is the test nino with a Business Request")
      val res = IncomeTaxSubscription.createSubscription(feBusinessRequest)

      Then("The result should have a HTTP status of OK and a body containing the MTDID")
      res should have(
        httpStatus(OK),
        jsonBodyAs[FESuccessResponse](FESuccessResponse(Some(testMtditId)))
      )

      Then("The subscription should have been audited")
      AuditStub.verifyAudit()
    }
    "call the subscription service successfully when auth succeeds for a property registration" in {
      Given("I setup the wiremock stubs")
      AuthStub.stubAuthSuccess()
      RegistrationStub.stubNewRegistrationSuccess()
      SubscriptionStub.stubPropertySubscribeSuccess()


      When("I call POST /subscription/:nino where nino is the test nino with a Property Request")
      val res = IncomeTaxSubscription.createSubscription(fePropertyRequest)

      Then("The result should have a HTTP status of OK and a body containing the MTDID")
      res should have(
        httpStatus(OK),
        jsonBodyAs[FESuccessResponse](FESuccessResponse(Some(testMtditId)))
      )

      Then("The subscription should have been audited")
      AuditStub.verifyAudit()
    }

    "call the subscription service successfully when auth succeeds for a business and property registration" in {
      Given("I setup the wiremock stubs")
      AuthStub.stubAuthSuccess()
      RegistrationStub.stubNewRegistrationSuccess()
      SubscriptionStub.stubBusinessSubscribeSuccess()
      SubscriptionStub.stubPropertySubscribeSuccess()


      When("I call POST /subscription/:nino where nino is the test nino with both a property request and a business request")
      val res = IncomeTaxSubscription.createSubscription(feBothRequest)

      Then("The result should have a HTTP status of OK and a body containing the MTDID")
      res should have(
        httpStatus(OK),
        jsonBodyAs[FESuccessResponse](FESuccessResponse(Some(testMtditId)))
      )

      Then("Business subscription should have been called")
      SubscriptionStub.verifyBusinessSubscribe()

      Then("Property subscription should have been called")
      SubscriptionStub.verifyPropertySubscribe()

      Then("The subscription should have been audited")
      AuditStub.verifyAudit()
    }

    "fail when Auth returns an UNAUTHORIZED response" in {
      Given("I setup the wiremock stubs")
      AuthStub.stubAuthFailure()

      When("I call POST /subscription/:nino where nino is the test nino with a Business Request")
      val res = IncomeTaxSubscription.createSubscription(feBusinessRequest)

      Then("The result should have a HTTP status of UNAUTHORIZED and an empty body")
      res should have(
        httpStatus(UNAUTHORIZED)
      )
    }

    "fail when Registration returns a BAD_REQUEST response" in {
      Given("I setup the wiremock stubs")
      AuthStub.stubAuthSuccess()
      RegistrationStub.stubNewRegistrationFailure()

      When("I call POST /subscription/:nino where nino is the test nino with a Business Request")
      val res = IncomeTaxSubscription.createSubscription(feBusinessRequest)

      Then("The result should have a HTTP status of BAD_REQUEST and a reason code body")
      res should have(
        httpStatus(BAD_REQUEST),
        jsonBodyAs[FEFailureResponse](FEFailureResponse(testErrorReason))
      )

      Then("The subscription should have been audited")
      AuditStub.verifyAudit()

    }

    "fail when Business Subscription returns a BAD_REQUEST response" in {
      Given("I setup the wiremock stubs")
      AuthStub.stubAuthSuccess()
      RegistrationStub.stubNewRegistrationSuccess()
      SubscriptionStub.stubBusinessSubscribeFailure()

      When("I call POST /subscription/:nino where nino is the test nino with a Business Request")
      val res = IncomeTaxSubscription.createSubscription(feBusinessRequest)

      Then("The result should have a HTTP status of BAD_REQUEST and a reason code body")
      res should have(
        httpStatus(BAD_REQUEST),
        jsonBodyAs[FEFailureResponse](FEFailureResponse(testErrorReason))
      )

      Then("The subscription should have been audited")
      AuditStub.verifyAudit()

    }

    "fail when Property Subscription returns a NOT_FOUND response" in {
      Given("I setup the wiremock stubs")
      AuthStub.stubAuthSuccess()
      RegistrationStub.stubNewRegistrationSuccess()
      SubscriptionStub.stubPropertySubscribeFailure()

      When("I call POST /subscription/:nino where nino is the test nino with a Property Request")
      val res = IncomeTaxSubscription.createSubscription(fePropertyRequest)

      Then("The result should have a HTTP status of NOT_FOUND and a reason code body")
      res should have(
        httpStatus(NOT_FOUND),
        jsonBodyAs[FEFailureResponse](FEFailureResponse(testErrorReason))
      )

      Then("The subscription should have been audited")
      AuditStub.verifyAudit()

    }

    "fail when BOTH Subscriptions returns NOT_FOUND responses during a dual Subscription" in {
      Given("I setup the wiremock stubs")
      AuthStub.stubAuthSuccess()
      RegistrationStub.stubNewRegistrationSuccess()
      SubscriptionStub.stubBusinessSubscribeFailure()
      SubscriptionStub.stubPropertySubscribeFailure()

      When("I call POST /subscription/:nino where nino is the test nino with both a property request and a business request")
      val res = IncomeTaxSubscription.createSubscription(feBothRequest)

      Then("The result should have a HTTP status of NOT_FOUND and return a reason code")
      res should have(
        httpStatus(BAD_REQUEST),
        jsonBodyAs[FEFailureResponse](FEFailureResponse(testErrorReason))
      )

      Then("Business subscription should have been called")
      SubscriptionStub.verifyBusinessSubscribe()

      Then("Property subscription should have been called")
      SubscriptionStub.verifyPropertySubscribe()

      Then("The subscription should have been audited")
      AuditStub.verifyAudit()
    }

    "fail when Business Subscription returns BAD_REQUEST but Property Subscription has no errors during a BOTH Subscription" in {
      Given("I setup the wiremock stubs")
      AuthStub.stubAuthSuccess()
      RegistrationStub.stubNewRegistrationSuccess()
      SubscriptionStub.stubBusinessSubscribeFailure()
      SubscriptionStub.stubPropertySubscribeSuccess()

      When("I call POST /subscription/:nino where nino is the test nino with both a property request and a business request")
      val res = IncomeTaxSubscription.createSubscription(feBothRequest)

      Then("The Business Subscription result should have a HTTP status of BAD_REQUEST and return a reason code")
      res should have(
        httpStatus(BAD_REQUEST),
        jsonBodyAs[FEFailureResponse](FEFailureResponse(testErrorReason))
      )

      Then("Business subscription should have been called")
      SubscriptionStub.verifyBusinessSubscribe()

      Then("Property subscription should have been called")
      SubscriptionStub.verifyPropertySubscribe()

      Then("The subscription should have been audited")
      AuditStub.verifyAudit()
    }

    "fail when Property Subscription returns BAD_REQUEST but Business Subscription has no errors during a BOTH Subscription" in {
      Given("I setup the wiremock stubs")
      AuthStub.stubAuthSuccess()
      RegistrationStub.stubNewRegistrationSuccess()
      SubscriptionStub.stubBusinessSubscribeSuccess()
      SubscriptionStub.stubPropertySubscribeFailure()

      When("I call POST /subscription/:nino where nino is the test nino with both a property request and a business request")
      val res = IncomeTaxSubscription.createSubscription(feBothRequest)

      Then("The Property Subscription result should have a HTTP status of NOT_FOUND and return a reason code")
      res should have(
        httpStatus(NOT_FOUND),
        jsonBodyAs[FEFailureResponse](FEFailureResponse(testErrorReason))
      )

      Then("Business subscription should have been called")
      SubscriptionStub.verifyBusinessSubscribe()

      Then("Property subscription should have been called")
      SubscriptionStub.verifyPropertySubscribe()

      Then("The subscription should have been audited")
      AuditStub.verifyAudit()
    }

  }
}
