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

package models.subscription

import models.subscription.IncomeSourceModel
import models.subscription.business._
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec
import utils.{Implicits, Resources}
import utils.TestConstants._
import utils.TestConstants.BusinessSubscriptionResponse.successResponse

class BusinessSubscriptionResponseModelSpec extends UnitSpec with Implicits {

  "BusinessSubscriptionResponseModel" should {

    val failedReason = "Service unavailable"
    val failedCode = "SERVICE_UNAVAILABLE"
    val testSuccessResponse = successResponse(testSafeId, testMtditId, testSourceId)
    val testFailedResponse = failureResponse(failedCode, failedReason)

    "for a successful response" should {
      "Read the safe id, mtdId and Income Sources correctly from a successful business subscription response" in {
        val expected = BusinessSubscriptionSuccessResponseModel(testSafeId, testMtditId, List(IncomeSourceModel(testSourceId)))
        Json.fromJson[BusinessSubscriptionSuccessResponseModel](testSuccessResponse).get shouldBe expected
      }

      "Be valid against the schema" in {
        Resources.validateJson(Resources.businessSubscriptionResponseSchema, testSuccessResponse) shouldBe true
      }
    }

    "for a failed response" should {

      "Read the error messages correctly from a failed business subscription response" in {
        val expected = BusinessSubscriptionErrorResponseModel(failedCode, failedReason)
        Json.fromJson[BusinessSubscriptionErrorResponseModel](testFailedResponse).get shouldBe expected
      }

      "Be valid against the schema" in {
        Resources.validateJson(Resources.businessSubscriptionResponseSchema, testFailedResponse) shouldBe true
      }
    }
  }
}
