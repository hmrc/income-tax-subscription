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

package controllers.matching

import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks._
import models.lockout.LockoutRequest
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status._
import play.modules.reactivemongo.ReactiveMongoComponent
import repositories.LockoutMongoRepository

import scala.concurrent.ExecutionContext.Implicits.global

class LockoutStatusControllerISpec extends ComponentSpecBase with BeforeAndAfterEach {
  implicit lazy val mongo = app.injector.instanceOf[ReactiveMongoComponent]

  object TestLockoutMongoRepository extends LockoutMongoRepository

  override def beforeEach(): Unit = {
    await(TestLockoutMongoRepository.drop)
  }

  "checkLockoutStatus" should {
    "call the lockout status service successfully when lock exists" in {
      Given("I setup the wiremock stubs")
      AuthStub.stubAuthSuccess()

      def insert = TestLockoutMongoRepository.lockoutAgent(testArn, 10)

      await(insert).isDefined shouldBe true

      When("I call GET /client-matching/lock/:arn where arn is the test arn and lock exists")
      val res = IncomeTaxSubscription.checkLockoutStatus(testArn)

      Then("The result should have a HTTP status of OK")
      res should have(
        httpStatus(OK)
      )

    }
    "call the lockout status service successfully when lock doesn't exists" in {
      Given("I setup the wiremock stubs")
      AuthStub.stubAuthSuccess()

      When("I call GET /client-matching/lock/:arn where arn is the test arn and lock doesn't exists")
      val res = IncomeTaxSubscription.checkLockoutStatus(testArn)

      Then("The result should have a HTTP status of NOT_FOUND")
      res should have(
        httpStatus(NOT_FOUND)
      )

    }
  }
  "lockoutAgent" should {
    "lockout the agent when tries have exceeded" in {
      Given("I setup the wiremock stubs")
      AuthStub.stubAuthSuccess()

      def lockoutRequest = LockoutRequest(30)

      When("I call POST /client-matching/lock/:arn where arn is the test arn")
      val res = IncomeTaxSubscription.lockoutAgent(testArn, lockoutRequest)

      Then("The result should have a HTTP status of OK")
      res should have(
        httpStatus(CREATED)
      )

    }
    "returns an error if lock already exists" in {
      def lockoutRequest = LockoutRequest(30)

      Given("I setup the wiremock stubs")
      AuthStub.stubAuthSuccess()

      And("There is a previous lock set for the user")
      IncomeTaxSubscription.lockoutAgent(testArn, lockoutRequest)

      When("I call POST /client-matching/lock/:arn where arn is the test arn")
      def res = IncomeTaxSubscription.lockoutAgent(testArn, lockoutRequest)

      Then("The result should have a HTTP status of INTERNAL_SERVER_ERROR")
      res should have(
        httpStatus(INTERNAL_SERVER_ERROR)
      )

    }
  }
}
