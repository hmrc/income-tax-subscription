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


import common.CommonSpec
import config.MicroserviceAppConfig
import models.SignUpResponse.SignUpSuccess
import models.monitoring.{RegistrationFailureAudit, RegistrationSuccessAudit}
import models.{ErrorModel, SignUpRequest, SignUpResponse}
import org.mockito.Mockito.when
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, UNPROCESSABLE_ENTITY}
import play.api.libs.json.Json
import play.api.mvc.{ControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, contentAsString, defaultAwaitTimeout, status, stubControllerComponents}
import services.mocks.monitoring.MockAuditService
import services.mocks.{MockAuthService, MockHIPSignUpTaxYearConnector}
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}
import utils.TestConstants.{hmrcAsAgent, testNino, testTaxYear, testTaxYearSignUpSubmission, testUtr}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SignUpControllerSpec extends CommonSpec
  with MockAuthService
  with MockHIPSignUpTaxYearConnector
  with MockAuditService
  with GuiceOneAppPerSuite {

  lazy val mockCC: ControllerComponents = stubControllerComponents()
  lazy val mockAppConfig: MicroserviceAppConfig = mock[MicroserviceAppConfig]

  object TestController extends SignUpController(
    mockAuthService,
    mockAuditService,
    mockHIPSignUpTaxYearConnector,
    mockCC,
    mockAppConfig
  ) {
    when(mockAppConfig.getHipAuthToken).thenReturn(
      auth
    )
  }

  val auth = "auth"

  lazy val testSignUpRequest: SignUpRequest = SignUpRequest(nino = testNino, utr = testUtr, taxYear = testTaxYear)

  "SignUpController" should {
    "return OK with the sign up successful response" when {
      "sign up was successful" when {
        "an individual signs themselves up" in {
          val fakeRequest = FakeRequest().withBody(Json.toJson(testTaxYearSignUpSubmission(testNino, testUtr, testTaxYear)))

          mockRetrievalSuccess(Enrolments(Set()))
          hipSignUpTaxYear(testSignUpRequest)(Future.successful(Right(SignUpSuccess("XAIT000000"))))

          val result: Future[Result] = TestController.signUp(fakeRequest)

          status(result) shouldBe OK
          contentAsJson(result) shouldBe Json.obj(
            "mtdbsa" -> "XAIT000000"
          )
          verifyAudit(RegistrationSuccessAudit(None, testNino, "XAIT000000", auth, None))
        }
        "an agent signs up their client" in {
          val fakeRequest = FakeRequest().withBody(Json.toJson(testTaxYearSignUpSubmission(testNino, testUtr, testTaxYear)))

          mockRetrievalSuccess(Enrolments(Set(Enrolment(hmrcAsAgent, Seq(EnrolmentIdentifier("AgentReferenceNumber", "123456789")), "Activated"))))
          hipSignUpTaxYear(testSignUpRequest)(Future.successful(Right(SignUpSuccess("XAIT000000"))))

          val result: Future[Result] = TestController.signUp(fakeRequest)

          status(result) shouldBe OK
          contentAsJson(result) shouldBe Json.obj(
            "mtdbsa" -> "XAIT000000"
          )
          verifyAudit(RegistrationSuccessAudit(Some("123456789"), testNino, "XAIT000000", auth, None))
        }
      }
    }
    "return UnprocessableEntity" when {
      "the customer is already signed up" in {
        val fakeRequest = FakeRequest().withBody(Json.toJson(testTaxYearSignUpSubmission(testNino, testUtr, testTaxYear)))

        mockRetrievalSuccess(Enrolments(Set()))
        hipSignUpTaxYear(testSignUpRequest)(Future.successful(Right(SignUpResponse.AlreadySignedUp)))

        val result: Future[Result] = TestController.signUp(fakeRequest)

        status(result) shouldBe UNPROCESSABLE_ENTITY
        contentAsString(result) shouldBe "Customer already signed up"
      }
    }
    "return InternalServerError" when {
      "sign up was unsuccessful and an error was returned" in {
        val fakeRequest = FakeRequest().withBody(Json.toJson(testTaxYearSignUpSubmission(testNino, testUtr, testTaxYear)))

        mockRetrievalSuccess(Enrolments(Set(Enrolment(hmrcAsAgent, Seq(EnrolmentIdentifier("AgentReferenceNumber", "123456789")), "Activated"))))
        hipSignUpTaxYear(testSignUpRequest)(Future.successful(Left(ErrorModel(INTERNAL_SERVER_ERROR, "Failure"))))

        val result = TestController.signUp(fakeRequest)

        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentAsString(result) shouldBe "Failed Sign up"
        verifyAudit(RegistrationFailureAudit(testNino, INTERNAL_SERVER_ERROR, "Failure"))
      }
    }
  }
}
