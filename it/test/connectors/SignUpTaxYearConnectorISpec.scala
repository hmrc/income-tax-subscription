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
import models.{ErrorModel, SignUpResponse}
import play.api.http.Status._
import play.api.mvc.Request
import play.api.test.FakeRequest

class SignUpTaxYearConnectorISpec extends ComponentSpecBase {


  private lazy val signUpConnector: SignUpTaxYearConnector = app.injector.instanceOf[SignUpTaxYearConnector]
  private lazy val appConfig: MicroserviceAppConfig = app.injector.instanceOf[MicroserviceAppConfig]
  implicit val request: Request[_] = FakeRequest()

  "The sign up tax year connector" when {
    "receiving a 200 response" should {
      "return a valid MTDBSA number when valid json is found" in {
        SignUpTaxYearStub.stubSignUp(testTaxYearSignUpSubmission(testNino, testTaxYear), appConfig.signUpServiceAuthorisationToken, appConfig.signUpServiceEnvironment)(
          OK, testSignUpSuccessBody
        )

        val result = signUpConnector.signUp(testNino, testTaxYear)

        result.futureValue shouldBe Right(SignUpResponse("XQIT00000000001"))
      }

      "return a Json parse failure when invalid json is found" in {
        SignUpTaxYearStub.stubSignUp(testTaxYearSignUpSubmission(testNino, testTaxYear), appConfig.signUpServiceAuthorisationToken, appConfig.signUpServiceEnvironment)(
          OK, testSignUpInvalidBody
        )

        val result = signUpConnector.signUp(testNino, testTaxYear)

        result.futureValue shouldBe Left(ErrorModel(status = 200, "Failed to read Json for MTD Sign Up Response"))
      }
    }

    "receiving a non-200 response" should {
      "return the status and error received" in {
        SignUpTaxYearStub.stubSignUp(testTaxYearSignUpSubmission(testNino, testTaxYear), appConfig.signUpServiceAuthorisationToken, appConfig.signUpServiceEnvironment)(
          INTERNAL_SERVER_ERROR, failureResponse("code", "reason")
        )

        val result = signUpConnector.signUp(testNino, testTaxYear)

        result.futureValue shouldBe Left(ErrorModel(INTERNAL_SERVER_ERROR, """{"code":"code","reason":"reason"}"""))
      }
    }
  }
}

