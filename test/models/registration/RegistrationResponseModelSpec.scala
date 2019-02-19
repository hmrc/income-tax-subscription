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

package models.registration

import models.registration._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.test.UnitSpec
import utils.Implicits

class RegistrationResponseModelSpec extends UnitSpec with Implicits {

  "NewRegistrationResponseModel" should {
    import utils.TestConstants.NewRegistrationResponse.{failureResponse, successResponse}

    "Reads the safe id correctly from a successful registration response" in {
      val safeId = "XE0001234567890"
      val response: JsValue = successResponse(safeId)
      Json.fromJson[RegistrationSuccessResponseModel](response).get shouldBe RegistrationSuccessResponseModel(safeId)
    }

    "Reads the error messages correctly from a failure registration response" in {
      val reason = "Service unavailable"
      val code = "SERVICE_UNAVAILABLE"
      val response: JsValue = failureResponse(code, reason)
      Json.fromJson[NewRegistrationFailureResponseModel](response).get shouldBe NewRegistrationFailureResponseModel(code, reason)
    }
  }

  "GetRegistrationResponseModel" should {
    import utils.TestConstants.GetRegistrationResponse.{failureResponse, successResponse}

    "Reads the safe id correctly from a successful registration response" in {
      val safeId = "XE0001234567890"
      val response: JsValue = successResponse(safeId)
      Json.fromJson[RegistrationSuccessResponseModel](response).get shouldBe RegistrationSuccessResponseModel(safeId)
    }

    "Reads the error messages correctly from a failure registration response" in {
      val reason = "Service unavailable"
      val response: JsValue = failureResponse(reason)
      Json.fromJson[GetRegistrationFailureResponseModel](response).get shouldBe GetRegistrationFailureResponseModel(reason)
    }
  }

}
