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
import config.featureswitch.FeatureSwitching
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.{AuthStub, SignUpTaxYearStub}
import models.SignUpResponse.SignUpSuccess
import play.api.Configuration
import play.api.http.Status._

class SignUpControllerISpec extends ComponentSpecBase with FeatureSwitching {

  val appConfig: MicroserviceAppConfig = app.injector.instanceOf[MicroserviceAppConfig]
  val signUpController: SignUpController = app.injector.instanceOf[SignUpController]
  val configuration: Configuration = app.injector.instanceOf[Configuration]

  "signUp" should {

    "call sign up connector successfully when auth succeeds for a sign up submission 200" in {
      AuthStub.stubAuth(OK)
      SignUpTaxYearStub.stubSignUp(
        testTaxYearSignUpSubmission(testNino, testTaxYear),
        appConfig.signUpServiceAuthorisationToken,
        appConfig.signUpServiceEnvironment
      )(OK, testSignUpSuccessBody)

      val res = IncomeTaxSubscription.signUp(testNino, testTaxYear)

      res should have(
        httpStatus(OK)
      )
      res should have(
        jsonBodyAs[SignUpSuccess](SignUpSuccess("XQIT00000000001"))
      )
    }

    "return a Json parse failure when invalid json is found" in {
      AuthStub.stubAuthSuccess()
      SignUpTaxYearStub.stubSignUp(
        testTaxYearSignUpSubmission(testNino, testTaxYear),
        appConfig.signUpServiceAuthorisationToken,
        appConfig.signUpServiceEnvironment
      )(OK, testSignUpInvalidBody)

      val res = IncomeTaxSubscription.signUp(testNino, testTaxYear)

      res should have(
        httpStatus(INTERNAL_SERVER_ERROR)
      )
    }

    "Show error processing Sign up request with status Internal Server Error" in {
      AuthStub.stubAuthSuccess()
      SignUpTaxYearStub.stubSignUp(
        testTaxYearSignUpSubmission(testNino, testTaxYear),
        appConfig.signUpServiceAuthorisationToken,
        appConfig.signUpServiceEnvironment
      )(INTERNAL_SERVER_ERROR, failureResponse("code", "reason"))

      val res = IncomeTaxSubscription.signUp(testNino, testTaxYear)

      res should have(
        httpStatus(INTERNAL_SERVER_ERROR)
      )
    }
  }
}