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

package unit.models.subscription

import models.subscription.business._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.test.UnitSpec
import utils.Implicits
import utils.TestConstants._

class BusinessSubscriptionResponseModelSpec extends UnitSpec with Implicits {

  "NewRegistrationResponseModel" should {
    import utils.TestConstants.BusinessSubscriptionResponse.{failureResponse, successResponse}

    "Reads the safe id, mtdId and Income Sources correctly from a successful registration response" in {
      val response: JsValue = successResponse(testSafeId, testMtditId, testSourceId)
      val expected = BusinessSubscriptionSuccessResponseModel(testSafeId, testMtditId, List(IncomeSourceModel(testSourceId)))
      Json.fromJson[BusinessSubscriptionSuccessResponseModel](response).get shouldBe expected
    }

    "Reads the error messages correctly from a failure registration response" in {
      val reason = "Service unavailable"
      val code = "SERVICE_UNAVAILABLE"
      val response: JsValue = failureResponse(code, reason)
      Json.fromJson[BusinessSubscriptionErrorResponseModel](response).get shouldBe BusinessSubscriptionErrorResponseModel(code, reason)
    }
  }
}
