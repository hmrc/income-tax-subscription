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
import helpers.IntegrationTestConstants._
import helpers.WiremockHelper.StubResponse
import helpers.servicemocks.HIPSignUpTaxYearStub
import helpers.{ComponentSpecBase, WiremockHelper}
import models.SignUpResponse.{AlreadySignedUp, SignUpSuccess}
import models.{ErrorModel, SignUpRequest}
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.mvc.Request
import play.api.test.FakeRequest
import utils.TestConstants.{testNino, testTaxYear, testUtr}

class HIPSignUpTaxYearConnectorISpec extends ComponentSpecBase {

  private lazy val signUpConnector: HIPSignUpTaxYearConnector = app.injector.instanceOf[HIPSignUpTaxYearConnector]
  lazy val appConfig: MicroserviceAppConfig = app.injector.instanceOf[MicroserviceAppConfig]
  implicit val request: Request[_] = FakeRequest()

  lazy val testSignUpRequest: SignUpRequest = SignUpRequest(nino = testNino, utr = testUtr, taxYear = testTaxYear)

  "The sign up tax year connector" when {
    "receiving a 201 response" should {
      "return a valid MTDBSA number when valid json is found and utr is submitted" in {
        HIPSignUpTaxYearStub.stubSignUp(
          hipTestTaxYearSignUpRequestBodyWithUtr(testNino, testUtr, testTaxYear)
        )(
          CREATED, hipTestSignUpSuccessBody
        )

        val result = signUpConnector.signUp(testSignUpRequest)

        result.futureValue shouldBe Right(SignUpSuccess("XQIT00000000001"))
      }

      "return a Json parse failure when invalid json is found" in {
        HIPSignUpTaxYearStub.stubSignUp(
          hipTestTaxYearSignUpRequestBodyWithUtr(testNino, testUtr, testTaxYear)
        )(
          CREATED, hipTestSignUpInvalidBody
        )

        val result = signUpConnector.signUp(testSignUpRequest)

        result.futureValue shouldBe Left(
          ErrorModel(status = CREATED, "API #5317: Sign-up - Status: 201, Message: Failure parsing json response")
        )
      }
    }

    "receiving a 422 response with a customer already signed up code" should {
      "return a already signed up result" in {
        HIPSignUpTaxYearStub.stubSignUp(
          hipTestTaxYearSignUpRequestBodyWithUtr(testNino, testUtr, testTaxYear)
        )(
          status = UNPROCESSABLE_ENTITY,
          body = Json.obj("errors" ->
            Json.obj("code" -> "820", "text" -> "The customer is already signed up", "processingDate" -> "2022-01-31T09:26:17Z")
          )
        )

        val result = signUpConnector.signUp(testSignUpRequest)

        result.futureValue shouldBe Right(AlreadySignedUp)
      }

      "return a Json parse failure when invalid json is found" in {
        HIPSignUpTaxYearStub.stubSignUp(
          hipTestTaxYearSignUpRequestBodyWithUtr(testNino, testUtr, testTaxYear)
        )(
          UNPROCESSABLE_ENTITY, testSignUpInvalidBody
        )

        val result = signUpConnector.signUp(testSignUpRequest)

        result.futureValue shouldBe Left(
          ErrorModel(status = UNPROCESSABLE_ENTITY, "API #5317: Sign-up - Status: 422, Message: Failure parsing json response")
        )
      }
    }

    "receiving a 422 response without customer already signed up code" should {
      "return the status and error received" in {
        HIPSignUpTaxYearStub.stubSignUp(
          hipTestTaxYearSignUpRequestBodyWithUtr(testNino, testUtr, testTaxYear)
        )(
          status = UNPROCESSABLE_ENTITY,
          body = Json.obj("errors" ->
            Json.obj("code" -> "002", "text" -> "ID not found", "processingDate" -> "2022-01-31T09:26:17Z")
          )
        )

        val result = signUpConnector.signUp(testSignUpRequest)

        result.futureValue shouldBe Left(
          ErrorModel(UNPROCESSABLE_ENTITY, "API #5317: Sign-up - Status: 422, Code: 002, Reason: ID not found")
        )
      }
    }

    "receiving a 500 response" should {
      "return the status and error received" in {
        HIPSignUpTaxYearStub.stubSignUp(
          hipTestTaxYearSignUpRequestBodyWithUtr(testNino, testUtr, testTaxYear)
        )(
          status = INTERNAL_SERVER_ERROR,
          body = Json.obj("error" ->
            Json.obj("code" -> "500", "message" -> "Server Error", "logID" -> "C0000AB8190C8E1F000000C700006836")
          )
        )

        val result = signUpConnector.signUp(testSignUpRequest)

        result.futureValue shouldBe Left(
          ErrorModel(INTERNAL_SERVER_ERROR, "API #5317: Sign-up - Status: 500, Message: Unexpected status returned: INTERNAL_SERVER_ERROR")
        )
      }
    }

    "receiving a 403 response" should {
      s"handle consecutive $FORBIDDEN responses and then succeed" in {
        WiremockHelper.stubPostSequence(s"/etmp/RESTAdapter/itsa/taxpayer/signup-mtdfb")(
          StubResponse(FORBIDDEN),
          StubResponse(FORBIDDEN),
          StubResponse(CREATED, hipTestSignUpSuccessBody)
        )

        val result = signUpConnector.signUp(testSignUpRequest)

        result.futureValue shouldBe Right(SignUpSuccess("XQIT00000000001"))

        WiremockHelper.verifyPost(
          uri = s"/etmp/RESTAdapter/itsa/taxpayer/signup-mtdfb",
          times = 3
        )
      }
    }
  }
}
