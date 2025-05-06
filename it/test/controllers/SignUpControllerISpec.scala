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
import config.featureswitch.{FeatureSwitching, SubmitUtrToSignUp, UseHIPSignUpTaxYearAPI}
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.{AuthStub, HIPSignUpTaxYearStub, SignUpTaxYearStub}
import models.SignUpRequest
import models.SignUpResponse.SignUpSuccess
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.Json
import utils.TestConstants.testUtr

class SignUpControllerISpec extends ComponentSpecBase with FeatureSwitching {

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(SubmitUtrToSignUp)
    disable(UseHIPSignUpTaxYearAPI)
  }

  val appConfig: MicroserviceAppConfig = app.injector.instanceOf[MicroserviceAppConfig]
  val signUpController: SignUpController = app.injector.instanceOf[SignUpController]
  val configuration: Configuration = app.injector.instanceOf[Configuration]

  lazy val testSignUpRequest = SignUpRequest(testNino, testUtr, testTaxYear)

  "signUp" when {
    "the SubmitUtrToSignUp feature switch is enabled" should {
      "call sign up connector successfully when auth succeeds for a sign up submission 200" in {
        enable(SubmitUtrToSignUp)

        AuthStub.stubAuth(OK)
        SignUpTaxYearStub.stubSignUp(
          testTaxYearSignUpRequestBodyWithUtr(testNino, testUtr, testTaxYear),
          appConfig.signUpServiceAuthorisationToken,
          appConfig.signUpServiceEnvironment
        )(OK, testSignUpSuccessBody)

        val res = IncomeTaxSubscription.signUp(testSignUpRequest)

        res should have(
          httpStatus(OK)
        )
        res should have(
          jsonBodyAs[SignUpSuccess](SignUpSuccess("XQIT00000000001"))
        )
      }

      "return a Json parse failure when invalid json is found" in {
        enable(SubmitUtrToSignUp)

        AuthStub.stubAuthSuccess()
        SignUpTaxYearStub.stubSignUp(
          testTaxYearSignUpRequestBodyWithUtr(testNino, testUtr, testTaxYear),
          appConfig.signUpServiceAuthorisationToken,
          appConfig.signUpServiceEnvironment
        )(OK, testSignUpInvalidBody)

        val res = IncomeTaxSubscription.signUp(testSignUpRequest)

        res should have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }

      "Show error processing Sign up request with status Internal Server Error" in {
        enable(SubmitUtrToSignUp)

        AuthStub.stubAuthSuccess()
        SignUpTaxYearStub.stubSignUp(
          testTaxYearSignUpRequestBodyWithUtr(testNino, testUtr, testTaxYear),
          appConfig.signUpServiceAuthorisationToken,
          appConfig.signUpServiceEnvironment
        )(INTERNAL_SERVER_ERROR, failureResponse("code", "reason"))

        val res = IncomeTaxSubscription.signUp(testSignUpRequest)

        res should have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }

    "the UseHIPSignUpTaxYearAPI feature switch is enabled" should {
      "call sign up connector successfully when auth succeeds for a sign up submission 200" in {
        enable(SubmitUtrToSignUp)
        enable(UseHIPSignUpTaxYearAPI)

        AuthStub.stubAuth(OK)
        HIPSignUpTaxYearStub.stubSignUp(
          hipTestTaxYearSignUpRequestBodyWithUtr(testNino, testUtr, testTaxYear),
          appConfig.hipSignUpServiceAuthorisationToken
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
        enable(SubmitUtrToSignUp)
        enable(UseHIPSignUpTaxYearAPI)

        AuthStub.stubAuthSuccess()
        HIPSignUpTaxYearStub.stubSignUp(
          hipTestTaxYearSignUpRequestBodyWithUtr(testNino, testUtr, testTaxYear),
          appConfig.hipSignUpServiceAuthorisationToken
        )(CREATED, hipTestSignUpInvalidBody)

        val res = IncomeTaxSubscription.signUp(testSignUpRequest)

        res should have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }

      "Show error processing Sign up request with status Internal Server Error" in {
        enable(SubmitUtrToSignUp)
        enable(UseHIPSignUpTaxYearAPI)

        AuthStub.stubAuthSuccess()
        HIPSignUpTaxYearStub.stubSignUp(
          hipTestTaxYearSignUpRequestBodyWithUtr(testNino, testUtr, testTaxYear),
          appConfig.hipSignUpServiceAuthorisationToken
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

    "the SubmitUtrToSignUp feature switch is disabled" should {
      "call sign up connector successfully when auth succeeds for a sign up submission 200" in {
        AuthStub.stubAuth(OK)
        SignUpTaxYearStub.stubSignUp(
          testTaxYearSignUpRequestBody(testNino, testTaxYear),
          appConfig.signUpServiceAuthorisationToken,
          appConfig.signUpServiceEnvironment
        )(OK, testSignUpSuccessBody)

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
        SignUpTaxYearStub.stubSignUp(
          testTaxYearSignUpRequestBody(testNino, testTaxYear),
          appConfig.signUpServiceAuthorisationToken,
          appConfig.signUpServiceEnvironment
        )(OK, testSignUpInvalidBody)

        val res = IncomeTaxSubscription.signUp(testSignUpRequest)

        res should have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }

      "Show error processing Sign up request with status Internal Server Error" in {
        AuthStub.stubAuthSuccess()
        SignUpTaxYearStub.stubSignUp(
          testTaxYearSignUpRequestBody(testNino, testTaxYear),
          appConfig.signUpServiceAuthorisationToken,
          appConfig.signUpServiceEnvironment
        )(INTERNAL_SERVER_ERROR, failureResponse("code", "reason"))

        val res = IncomeTaxSubscription.signUp(testSignUpRequest)

        res should have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }
  }
}