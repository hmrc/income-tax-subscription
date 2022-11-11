/*
 * Copyright 2022 HM Revenue & Customs
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

import common.CommonSpec
import models.monitoring.{RegistrationFailureAudit, RegistrationSuccessAudit}
import models.{ErrorModel, SignUpResponse}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, contentAsString, defaultAwaitTimeout, status, stubControllerComponents}
import services.mocks.monitoring.MockAuditService
import services.mocks.{MockAuthService, MockSignUpConnector}
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}
import utils.MaterializerSupport
import utils.TestConstants.{hmrcAsAgent, testNino, testSignUpSubmission}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SignUpControllerSpec extends CommonSpec with MockAuthService with MockSignUpConnector with MaterializerSupport with MockAuditService {
  lazy val mockCC = stubControllerComponents()

  object TestController extends SignUpController(mockAuthService, mockAuditService, mockSignUpConnector, mockCC, appConfig)

  "Sign Up Controller" when {
    "signup is submitted" should {
      "return a 200 response" in {
        val fakeRequest = FakeRequest().withJsonBody(Json.toJson(testSignUpSubmission(testNino)))

        mockRetrievalSuccess(Enrolments(Set(Enrolment(hmrcAsAgent, Seq(EnrolmentIdentifier("AgentReferenceNumber", "123456789")), "Activated"))))
        signUp(testNino)(Future.successful(Right(SignUpResponse("XAIT000000"))))()


        val result = TestController.signUp(testNino)(fakeRequest)
        status(result) shouldBe OK
        contentAsJson(result).as[SignUpResponse].mtdbsa shouldBe {
          "XAIT000000"
        }
        verifyAudit(RegistrationSuccessAudit(Some("123456789"), testNino, "XAIT000000", "Bearer dev", None))
      }

    }
  }

  "Sign Up Controller" when {
    "signup is submitted" should {
      "return a Json parse failure when invalid json is found" in {
        val fakeRequest = FakeRequest().withJsonBody(Json.toJson(testSignUpSubmission(testNino)))

        mockRetrievalSuccess(Enrolments(Set(Enrolment(hmrcAsAgent, Seq(EnrolmentIdentifier("AgentReferenceNumber", "123456789")), "Activated"))))

        signUp(testNino)(Future.successful(Left(ErrorModel(OK, "Failed to read Json for MTD Sign Up Response"))))()

        val result = TestController.signUp(testNino)(fakeRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentAsString(result) shouldBe "Failed Sign up"
        verifyAudit(RegistrationFailureAudit(testNino, OK, "Failed to read Json for MTD Sign Up Response"))
      }
    }
  }

  "return InternalServerError" when {
    "signup is submitted" should {
      "return an error" in {
        val fakeRequest = FakeRequest().withJsonBody(Json.toJson(testSignUpSubmission(testNino)))

        mockRetrievalSuccess(Enrolments(Set(Enrolment(hmrcAsAgent, Seq(EnrolmentIdentifier("AgentReferenceNumber", "123456789")), "Activated"))))
        signUp(testNino)(Future.successful(Left(ErrorModel(INTERNAL_SERVER_ERROR, "Failure"))))()

        val result = TestController.signUp(testNino)(fakeRequest)

        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentAsString(result) shouldBe "Failed Sign up"
        verifyAudit(RegistrationFailureAudit(testNino, INTERNAL_SERVER_ERROR, "Failure"))
      }
    }
  }
}
