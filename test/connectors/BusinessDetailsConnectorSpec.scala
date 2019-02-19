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

import models.ErrorModel
import models.registration._
import play.api.http.Status._
import play.api.libs.json.JsValue
import connectors.mocks.TestBusinessDetailsConnector
import _root_.utils.JsonUtils._
import _root_.utils.TestConstants._
import uk.gov.hmrc.http.HeaderCarrier

class BusinessDetailsConnectorSpec extends TestBusinessDetailsConnector {

  implicit val hc = HeaderCarrier()

  lazy val env = appConfig.desEnvironment
  lazy val authToken = appConfig.desToken

  "BusinessDetailsConnector.getBusinessDetails" should {
    "Put in the correct headers" in {
      val rHc = TestBusinessDetailsConnector.createHeaderCarrierGet(hc)
      rHc.headers.contains("Authorization" -> s"Bearer $authToken") shouldBe true
      rHc.headers.contains("Environment" -> env) shouldBe true
    }

    "GET to the correct url" in {
      TestBusinessDetailsConnector.getBusinessDetailsUrl(testNino) should endWith(s"/registration/business-details/nino/$testNino")
    }

    def result = await(TestBusinessDetailsConnector.getBusinessDetails(testNino))

    "parse and return the success response correctly" in {
      mockBusinessDetails(getBusinessDetailsSuccess)
      result shouldBe Right(GetBusinessDetailsSuccessResponseModel(testMtditId))
    }

    "parse and return the Bad request response correctly" in {
      mockBusinessDetails(INVALID_NINO)
      result shouldBe Left(INVALID_NINO_MODEL)
    }

    "parse and return the Resource not found response correctly" in {
      mockBusinessDetails(NOT_FOUND_NINO)
      result shouldBe Left(NOT_FOUND_NINO_MODEL)
    }

    "parse and return the Conflict error response correctly" in {
      mockBusinessDetails(CONFLICT_ERROR)
      result shouldBe Left(CONFLICT_ERROR_MODEL)
    }

    "parse and return the Server error response correctly" in {
      mockBusinessDetails(SERVER_ERROR)
      result shouldBe Left(SERVER_ERROR_MODEL)
    }

    "parse and return the Service unavailable response correctly" in {
      mockBusinessDetails(UNAVAILABLE)
      result shouldBe Left(UNAVAILABLE_MODEL)
    }

    "return parse error for corrupt response" in {
      val corruptResponse: JsValue = """{"a": "not valid"}"""
      mockBusinessDetails((BAD_REQUEST, corruptResponse))
      result shouldBe Left(ErrorModel(INTERNAL_SERVER_ERROR, ErrorModel.parseFailure(corruptResponse)))
    }

  }

}
