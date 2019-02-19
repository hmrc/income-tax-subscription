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
import models.subscription.IncomeSourceModel
import models.subscription.business._
import models.subscription.property.PropertySubscriptionResponseModel
import play.api.http.Status._
import play.api.libs.json.JsValue
import connectors.mocks.TestSubscriptionConnector
import _root_.utils.JsonUtils._
import _root_.utils.TestConstants._

import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.hmrc.http.HeaderCarrier

class SubscriptionConnectorSpec extends TestSubscriptionConnector {

  implicit val hc = HeaderCarrier()

  "The connector should have the correct POST request headers for DES" in {
    val rHc = TestSubscriptionConnector.createHeaderCarrierPost(hc)
    rHc.headers.contains("Authorization" -> s"Bearer ${appConfig.desToken}") shouldBe true
    rHc.headers.contains("Content-Type" -> "application/json") shouldBe true
    rHc.headers.contains("Environment" -> appConfig.desEnvironment) shouldBe true
  }

  "SubscriptionConnector.businessSubscribe" should {

    def result = await(TestSubscriptionConnector.businessSubscribe(testNino, businessSubscriptionRequestPayload, arn = None))

    "Post to the correct url" in {
      TestSubscriptionConnector.businessSubscribeUrl(testNino) should endWith(s"/income-tax-self-assessment/nino/$testNino/business")
    }

    "parse and return the success response correctly" in {
      mockBusinessSubscribe(businessSubscriptionRequestPayload)(businessSubscribeSuccess)
      result shouldBe Right(BusinessSubscriptionSuccessResponseModel(testSafeId, testMtditId, List(IncomeSourceModel(testSourceId))))
    }

    "parse and return the Bad request response correctly" in {
      mockBusinessSubscribe(businessSubscriptionRequestPayload)(INVALID_NINO)
      result shouldBe Left(INVALID_NINO_MODEL)
    }

    "parse and return the Resource not found response correctly" in {
      mockBusinessSubscribe(businessSubscriptionRequestPayload)(NOT_FOUND_NINO)
      result shouldBe Left(NOT_FOUND_NINO_MODEL)
    }

    "parse and return the Conflict error response correctly" in {
      mockBusinessSubscribe(businessSubscriptionRequestPayload)(CONFLICT_ERROR)
      result shouldBe Left(CONFLICT_ERROR_MODEL)
    }

    "parse and return the Server error response correctly" in {
      mockBusinessSubscribe(businessSubscriptionRequestPayload)(SERVER_ERROR)
      result shouldBe Left(SERVER_ERROR_MODEL)
    }

    "parse and return the Service unavailable response correctly" in {
      mockBusinessSubscribe(businessSubscriptionRequestPayload)(UNAVAILABLE)
      result shouldBe Left(UNAVAILABLE_MODEL)
    }

    "return parse error for corrupt response" in {
      val corruptResponse: JsValue = """{"a": "not valid"}"""
      mockBusinessSubscribe(businessSubscriptionRequestPayload)((BAD_REQUEST, corruptResponse))
      result shouldBe Left(ErrorModel(INTERNAL_SERVER_ERROR, ErrorModel.parseFailure(corruptResponse)))
    }
  }

  "SubscriptionConnector.propertySubscribe" should {

    def result = await(TestSubscriptionConnector.propertySubscribe(testNino, arn = None))

    "Post to the correct url" in {
      TestSubscriptionConnector.propertySubscribeUrl(testNino) should endWith(s"/income-tax-self-assessment/nino/$testNino/properties")
    }

    "parse and return success response" in {
      mockPropertySubscribe(propertySubscribeSuccess)
      result shouldBe Right(PropertySubscriptionResponseModel(testSafeId, testMtditId, IncomeSourceModel(testSourceId)))
    }

    "parse and return Invalid Payload response" in {
      mockPropertySubscribe(INVALID_PAYLOAD)
      result shouldBe Left(INVALID_PAYLOAD_MODEL)
    }

    "parse and return Invalid Nino response" in {
      mockPropertySubscribe(INVALID_NINO)
      result shouldBe Left(INVALID_NINO_MODEL)
    }

    "parse and return not found response" in {
      mockPropertySubscribe(NOT_FOUND_NINO)
      result shouldBe Left(NOT_FOUND_NINO_MODEL)
    }

    "parse and return server error response" in {
      mockPropertySubscribe(SERVER_ERROR)
      result shouldBe Left(SERVER_ERROR_MODEL)
    }

    "parse and return service unavailable response" in {
      mockPropertySubscribe(UNAVAILABLE)
      result shouldBe Left(UNAVAILABLE_MODEL)
    }

    "return parse error for corrupt response" in {
      val corruptResponse: JsValue = """{"a": "not valid"}"""
      mockPropertySubscribe((BAD_REQUEST, corruptResponse))
      result shouldBe Left(ErrorModel(INTERNAL_SERVER_ERROR, ErrorModel.parseFailure(corruptResponse)))
    }
  }
}
