/*
 * Copyright 2019 HM Revenue & Customs
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

import _root_.utils.JsonUtils._
import _root_.utils.TestConstants._
import connectors.mocks.TestRegistrationConnector
import models.ErrorModel
import models.registration._
import play.api.http.Status._
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.HeaderCarrier

class RegistrationConnectorSpec extends TestRegistrationConnector {

  implicit val hc = HeaderCarrier()

  val env = appConfig.desEnvironment
  val authToken = appConfig.desToken

  "RegistrationConnector.register" should {
    "Put in the correct headers" in {
      val rHc = TestRegistrationConnector.createHeaderCarrierPost(hc)
      rHc.headers.contains("Authorization" -> s"Bearer $authToken") shouldBe true
      rHc.headers.contains("Content-Type" -> "application/json") shouldBe true
      rHc.headers.contains("Environment" -> env) shouldBe true
    }

    "Post to the correct url" in {
      TestRegistrationConnector.newRegistrationUrl(testNino) should endWith(s"/registration/individual/nino/$testNino")
    }

    def result = await(TestRegistrationConnector.register(testNino, registerRequestPayload))

    "parse and return the success response correctly" in {
      mockRegister(registerRequestPayload)(regSuccess)
      result shouldBe Right(RegistrationSuccessResponseModel(testSafeId))
    }

    "parse and return the Bad request response correctly" in {
      mockRegister(registerRequestPayload)(INVALID_NINO)
      result shouldBe Left(INVALID_NINO_MODEL)
    }

    "parse and return the Resource not found response correctly" in {
      mockRegister(registerRequestPayload)(NOT_FOUND_NINO)
      result shouldBe Left(NOT_FOUND_NINO_MODEL)
    }

    "parse and return the Conflict error response correctly" in {
      mockRegister(registerRequestPayload)(CONFLICT_ERROR)
      result shouldBe Left(CONFLICT_ERROR_MODEL)
    }

    "parse and return the Server error response correctly" in {
      mockRegister(registerRequestPayload)(SERVER_ERROR)
      result shouldBe Left(SERVER_ERROR_MODEL)
    }

    "parse and return the Service unavailable response correctly" in {
      mockRegister(registerRequestPayload)(UNAVAILABLE)
      result shouldBe Left(UNAVAILABLE_MODEL)
    }

    "return parse error for corrupt response" in {
      val corruptResponse: JsValue = """{"a": "not valid"}"""
      mockRegister(registerRequestPayload)((BAD_REQUEST, corruptResponse))
      result shouldBe Left(ErrorModel(INTERNAL_SERVER_ERROR, ErrorModel.parseFailure(corruptResponse)))
    }
  }

  "RegistrationConnector.getRegistration" should {

    "Put in the correct headers" in {
      val rHc = TestRegistrationConnector.createHeaderCarrierGet(hc)
      rHc.headers.contains("Authorization" -> s"Bearer $authToken") shouldBe true
      rHc.headers.contains("Environment" -> env) shouldBe true
    }

    "Post to the correct url" in {
      TestRegistrationConnector.getRegistrationUrl(testNino) should endWith(s"/registration/details?nino=$testNino")
    }

    def result = await(TestRegistrationConnector.getRegistration(testNino))

    "parse and return the success response correctly" in {
      mockGetRegistration(regSuccess)
      result shouldBe Right(RegistrationSuccessResponseModel(testSafeId))
    }

    "parse and return the Bad request response correctly" in {
      mockGetRegistration(INVALID_NINO)
      result shouldBe Left(ErrorModel(BAD_REQUEST, INVALID_NINO_MODEL.reason))
    }

    "parse and return the Resource not found response correctly" in {
      mockGetRegistration(NOT_FOUND_NINO)
      result shouldBe Left(ErrorModel(NOT_FOUND, NOT_FOUND_NINO_MODEL.reason))
    }

    "parse and return the Server error response correctly" in {
      mockGetRegistration(SERVER_ERROR)
      result shouldBe Left(ErrorModel(INTERNAL_SERVER_ERROR, SERVER_ERROR_MODEL.reason))
    }

    "parse and return the Service unavailable response correctly" in {
      mockGetRegistration(UNAVAILABLE)
      result shouldBe Left(ErrorModel(SERVICE_UNAVAILABLE, UNAVAILABLE_MODEL.reason))
    }

    "return parse error for corrupt response" in {
      val corruptResponse: JsValue = """{"a": "not valid"}"""
      mockGetRegistration((BAD_REQUEST, corruptResponse))
      result shouldBe Left(ErrorModel(INTERNAL_SERVER_ERROR, ErrorModel.parseFailure(corruptResponse)))
    }
  }

}
