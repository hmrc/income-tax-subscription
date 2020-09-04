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

package controllers


import controllers.Assets.OK
import models.{SignUpFailure, SignUpResponse}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.stubControllerComponents
import services.mocks.{MockAuthService, MockSignUpConnector}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import utils.TestConstants.{testNino, testSignUpSubmission}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SignUpControllerSpec extends UnitSpec with MockAuthService with MockSignUpConnector {
  lazy val mockCC = stubControllerComponents()


  object TestController extends SignUpController(mockAuthService, mockSignUpConnector, mockCC)

  "Sign Up Controller" when {
    "signup is submitted" should {
      "return a 200 response" in {
        val fakeRequest = FakeRequest().withJsonBody(Json.toJson(testSignUpSubmission(testNino)))
        implicit val hc: HeaderCarrier = HeaderCarrier()

        mockAuthSuccess()
        signUp(testNino)(Future.successful(Right(SignUpResponse("XAIT000000"))))


        val result = await(TestController.signUp(testNino)(fakeRequest))
        status(result) shouldBe OK
      }
    }
  }

  "Sign Up Controller" when {
    "signup is submitted" should {
      "return a Json parse failure when invalid json is found" in {
        val fakeRequest = FakeRequest().withJsonBody(Json.toJson(testSignUpSubmission(testNino)))
        implicit val hc: HeaderCarrier = HeaderCarrier()

        mockAuthSuccess()
        signUp(testNino)(Future.successful(Left(SignUpFailure(200,  "Failed to read Json for MTD Sign Up Response"))))


        val result = await(TestController.signUp(testNino)(fakeRequest))
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }



    "return InternalServerError" when {
    "signup is submitted" should {
      "return an error" in {
        val fakeRequest = FakeRequest().withJsonBody(Json.toJson(testSignUpSubmission(testNino)))
        implicit val hc = HeaderCarrier()

        mockAuthSuccess()
        signUp(testNino)(Future.successful(Left(SignUpFailure(INTERNAL_SERVER_ERROR, "Failure"))))

        val result = await(TestController.signUp(testNino)(fakeRequest))

        status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }
  }
}
