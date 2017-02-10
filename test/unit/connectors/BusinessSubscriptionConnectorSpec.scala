/*
 * Copyright 2017 HM Revenue & Customs
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

package unit.connectors

import models.ErrorModel
import models.subscription.business._
import play.api.http.Status._
import play.api.libs.json.JsValue
import uk.gov.hmrc.play.http.HeaderCarrier
import unit.connectors.mocks.MockBusinessSubscriptionConnector
import utils.JsonUtils._
import utils.TestConstants._
import BusinessSubscriptionResponse._

class BusinessSubscriptionConnectorSpec extends MockBusinessSubscriptionConnector {

  implicit val hc = HeaderCarrier()
  val env = config.getString("microservice.services.des.environment").get
  val authToken = config.getString("microservice.services.des.authorization-token").get

  val payload = BusinessSubscriptionRequestModel(
    BusinessDetailsModel(
      accountingPeriodStartDate = "2017-05-01",
      accountingPeriodEndDate = "2018-04-30",
      tradingName = "Test Business",
      cashOrAccruals = "cash"
    )
  )

  "BusinessSubscriptionConnector.businessSubscribe" should {
    "Have the correct request headers" in {
      val rHc = TestBusinessSubsscriptionConnector.createHeaderCarrierPost(hc)

      rHc.headers.contains("Authorization" -> s"Bearer $authToken") shouldBe true
      rHc.headers.contains("Content-Type" -> "application/json") shouldBe true
      rHc.headers.contains("Environment" -> env) shouldBe true
    }

    "Post to the correct url" in {
      TestBusinessSubsscriptionConnector.businessSubscribeUrl(testNino) should endWith(s"/income-tax-self-assessment/nino/$testNino/business")
    }

    def setupMockSubscribe(status: Int, response: JsValue): Unit =
      super.setupMockBusinessSubscribe(testNino, payload)(status, response)

    def call = await(TestBusinessSubsscriptionConnector.businessSubscribe(testNino, payload))

    "parse and return the success response correctly" in {
      setupMockSubscribe(OK, successResponse(testSafeId, testMtditId, testSourceId))
      val expected = Right(BusinessSubscriptionSuccessResponseModel(testSafeId, testMtditId, List(IncomeSourceModel(testSourceId))))
      val actual = call
      actual shouldBe expected
    }

    "parse and return the Bad request response correctly" in {
      val code = "INVALID_NINO"
      setupMockSubscribe(BAD_REQUEST, failureResponse(code, testErrorReason))
      val expected = Left(ErrorModel(BAD_REQUEST, BusinessSubscriptionErrorResponseModel(code, testErrorReason)))
      val actual = call
      actual shouldBe expected
    }

    "parse and return the Resource not found response correctly" in {
      val code = "NOT_FOUND"
      setupMockSubscribe(NOT_FOUND, failureResponse(code, testErrorReason))
      val expected = Left(ErrorModel(NOT_FOUND, BusinessSubscriptionErrorResponseModel(code, testErrorReason)))
      val actual = call
      actual shouldBe expected
    }

    "parse and return the Conflict error response correctly" in {
      val code = "CONFLICT"
      setupMockSubscribe(CONFLICT, failureResponse(code, testErrorReason))
      val expected = Left(ErrorModel(CONFLICT, BusinessSubscriptionErrorResponseModel(code, testErrorReason)))
      val actual = call
      actual shouldBe expected
    }

    "parse and return the Server error response correctly" in {
      val code = "SERVER_ERROR"
      setupMockSubscribe(INTERNAL_SERVER_ERROR, failureResponse(code, testErrorReason))
      val expected = Left(ErrorModel(INTERNAL_SERVER_ERROR, BusinessSubscriptionErrorResponseModel(code, testErrorReason)))
      val actual = call
      actual shouldBe expected
    }

    "parse and return the Service unavailable response correctly" in {
      val code = "SERVICE_UNAVAILABLE"
      setupMockSubscribe(SERVICE_UNAVAILABLE, failureResponse(code, testErrorReason))
      val expected = Left(ErrorModel(SERVICE_UNAVAILABLE, BusinessSubscriptionErrorResponseModel(code, testErrorReason)))
      val actual = call
      actual shouldBe expected
    }

    "return parse error for corrupt response" in {
      val corruptResponse: JsValue = """{"a": "not valid"}"""
      setupMockSubscribe(INTERNAL_SERVER_ERROR, corruptResponse)
      val expected = Left(ErrorModel(INTERNAL_SERVER_ERROR, ErrorModel.parseFailure(corruptResponse)))
      val actual = call
      actual shouldBe expected
    }
  }
}
