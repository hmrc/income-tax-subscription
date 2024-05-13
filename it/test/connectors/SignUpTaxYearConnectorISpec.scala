/*
 * Copyright 2020 HM Revenue & Customs
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

package connectors

import config.MicroserviceAppConfig
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.SignUpTaxYearStub
import models.ErrorModel
import models.SignUpResponse.{AlreadySignedUp, SignUpSuccess}
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.mvc.Request
import play.api.test.FakeRequest

class SignUpTaxYearConnectorISpec extends ComponentSpecBase {


  private lazy val signUpConnector: SignUpTaxYearConnector = app.injector.instanceOf[SignUpTaxYearConnector]
  private lazy val appConfig: MicroserviceAppConfig = app.injector.instanceOf[MicroserviceAppConfig]
  implicit val request: Request[_] = FakeRequest()

  "The sign up tax year connector" when {
    "receiving a 200 response" should {
      "return a valid MTDBSA number when valid json is found" in {
        SignUpTaxYearStub.stubSignUp(
          testTaxYearSignUpSubmission(testNino, testTaxYear),
          appConfig.signUpServiceAuthorisationToken,
          appConfig.signUpServiceEnvironment
        )(
          OK, testSignUpSuccessBody
        )

        val result = signUpConnector.signUp(testNino, testTaxYear)

        result.futureValue shouldBe Right(SignUpSuccess("XQIT00000000001"))
      }

      "return a Json parse failure when invalid json is found" in {
        SignUpTaxYearStub.stubSignUp(
          testTaxYearSignUpSubmission(testNino, testTaxYear),
          appConfig.signUpServiceAuthorisationToken,
          appConfig.signUpServiceEnvironment
        )(
          OK, testSignUpInvalidBody
        )

        val result = signUpConnector.signUp(testNino, testTaxYear)

        result.futureValue shouldBe Left(ErrorModel(status = OK, "Failed to read Json for MTD Sign Up Response"))
      }
    }

    "receiving a 422 response with a customer already signed up code" should {
      "return a already signed up result" in {
        SignUpTaxYearStub.stubSignUp(
          testTaxYearSignUpSubmission(testNino, testTaxYear),
          appConfig.signUpServiceAuthorisationToken,
          appConfig.signUpServiceEnvironment
        )(
          status = UNPROCESSABLE_ENTITY,
          body = Json.obj("failures" -> Json.arr(
            Json.obj("code" -> "CUSTOMER_ALREADY_SIGNED_UP", "reason" -> "The customer is already signed up")
          ))
        )

        val result = signUpConnector.signUp(testNino, testTaxYear)

        result.futureValue shouldBe Right(AlreadySignedUp)
      }

      "return a Json parse failure when invalid json is found" in {
        SignUpTaxYearStub.stubSignUp(
          testTaxYearSignUpSubmission(testNino, testTaxYear),
          appConfig.signUpServiceAuthorisationToken,
          appConfig.signUpServiceEnvironment
        )(
          UNPROCESSABLE_ENTITY, testSignUpInvalidBody
        )

        val result = signUpConnector.signUp(testNino, testTaxYear)

        result.futureValue shouldBe Left(ErrorModel(status = UNPROCESSABLE_ENTITY, "Failed to read Json for MTD Sign Up Response"))
      }
    }

    "receiving a 422 response without customer already signed up code" should {
      "return the status and error received" in {
        SignUpTaxYearStub.stubSignUp(
          testTaxYearSignUpSubmission(testNino, testTaxYear),
          appConfig.signUpServiceAuthorisationToken,
          appConfig.signUpServiceEnvironment
        )(
          UNPROCESSABLE_ENTITY, failureResponse("code", "reason")
        )

        val result = signUpConnector.signUp(testNino, testTaxYear)

        result.futureValue shouldBe Left(ErrorModel(UNPROCESSABLE_ENTITY, "code"))
      }
    }

    "receiving a 500 response" should {
      "return the status and error received" in {
        SignUpTaxYearStub.stubSignUp(
          testTaxYearSignUpSubmission(testNino, testTaxYear),
          appConfig.signUpServiceAuthorisationToken,
          appConfig.signUpServiceEnvironment
        )(
          INTERNAL_SERVER_ERROR, failureResponse("code", "reason")
        )

        val result = signUpConnector.signUp(testNino, testTaxYear)

        result.futureValue shouldBe Left(ErrorModel(INTERNAL_SERVER_ERROR, """{"failures":[{"code":"code","reason":"reason"}]}"""))
      }
    }
  }
}

