/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers

import config.MicroserviceAppConfig
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.{AuthStub, HIPSignUpTaxYearStub}
import models.SignUpRequest
import models.SignUpResponse.SignUpSuccess
import play.api.http.Status._
import play.api.libs.json.Json
import utils.TestConstants.testUtr

class SignUpControllerISpec extends ComponentSpecBase {

  val appConfig: MicroserviceAppConfig = app.injector.instanceOf[MicroserviceAppConfig]
  val signUpController: SignUpController = app.injector.instanceOf[SignUpController]

  lazy val testSignUpRequest: SignUpRequest =
    SignUpRequest(testNino, testUtr, testTaxYear)

  "signUp" when {
    "call sign up connector successfully when auth succeeds for a sign up submission 200" in {
      AuthStub.stubAuth(OK)
      HIPSignUpTaxYearStub.stubSignUp(
        hipTestTaxYearSignUpRequestBodyWithUtr(testNino, testUtr, testTaxYear)
      )(CREATED, hipTestSignUpSuccessBody)

      val res = IncomeTaxSubscription.signUp(testSignUpRequest)

      res should have(
        httpStatus(OK)
      )
      res should have(
        jsonBodyAs[SignUpSuccess](SignUpSuccess("XQIT00000000001"))
      )
    }

    "return a Json parse failure when invalid json is found" in {
      AuthStub.stubAuthSuccess()
      HIPSignUpTaxYearStub.stubSignUp(
        hipTestTaxYearSignUpRequestBodyWithUtr(testNino, testUtr, testTaxYear)
      )(CREATED, hipTestSignUpInvalidBody)

      val res = IncomeTaxSubscription.signUp(testSignUpRequest)

      res should have(
        httpStatus(INTERNAL_SERVER_ERROR)
      )
    }

    "Show error processing Sign up request with status Internal Server Error" in {
      AuthStub.stubAuthSuccess()
      HIPSignUpTaxYearStub.stubSignUp(
        hipTestTaxYearSignUpRequestBodyWithUtr(testNino, testUtr, testTaxYear)
      )(
        status = INTERNAL_SERVER_ERROR,
        body = Json.obj("error" ->
          Json.obj("code" -> "500", "message" -> "Server Error", "logID" -> "C0000AB8190C8E1F000000C700006836")
        )
      )

      val res = IncomeTaxSubscription.signUp(testSignUpRequest)

      res should have(
        httpStatus(INTERNAL_SERVER_ERROR)
      )
    }
  }
}