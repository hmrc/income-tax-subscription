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
import helpers.servicemocks.SignUpStub
import play.api.mvc.Request
import play.api.test.FakeRequest
import helpers.IntegrationTestConstants._
import models.{SignUpFailure, SignUpResponse}
import play.api.http.Status._

class SignUpConnectorISpec extends ComponentSpecBase {

  private lazy val signUpConnector: SignUpConnector = app.injector.instanceOf[SignUpConnector]
  private lazy val appConfig: MicroserviceAppConfig = app.injector.instanceOf[MicroserviceAppConfig]
  implicit val request: Request[_] = FakeRequest()

  "The sign up connector" when {

    "receiving a 200 response" should {

      "return a valid MTDBSA number when valid json is found" in {
        SignUpStub.stubSignUp(testNino, testSignUpSubmission(testNino), appConfig.desAuthorisationToken, appConfig.desEnvironment)(
          OK, testSignUpSuccessBody
        )

        val result = signUpConnector.signUp(testNino)

        await(result) shouldBe Right(SignUpResponse("XQIT00000000001"))
      }

      "return a Json parse failure when invalid json is found" in {
        SignUpStub.stubSignUp(testNino, testSignUpSubmission(testNino), appConfig.desAuthorisationToken, appConfig.desEnvironment)(
          OK, testSignUpInvalidBody
        )

        val result = signUpConnector.signUp(testNino)

        await(result) shouldBe Left(SignUpFailure(200,  "Failed to read Json for MTD Sign Up Response"))
      }
    }

    "receiving a non-200 response" should {

      "return the status and error received" in {
        SignUpStub.stubSignUp(testNino, testSignUpSubmission(testNino), appConfig.desAuthorisationToken, appConfig.desEnvironment)(
          INTERNAL_SERVER_ERROR, failureResponse("code", "reason")
        )

        val result = signUpConnector.signUp(testNino)

        await(result) shouldBe Left(SignUpFailure(INTERNAL_SERVER_ERROR, """{"code":"code","reason":"reason"}"""))
      }
    }
  }
}
