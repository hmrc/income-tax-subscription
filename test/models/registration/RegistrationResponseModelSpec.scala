/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.libs.json.{JsObject, JsSuccess, Json}
import uk.gov.hmrc.play.test.UnitSpec

class RegistrationResponseModelSpec extends UnitSpec {

  "GetBusinessDetailsSuccessResponseModel" should {

    val json: JsObject = Json.obj("mtdbsa" -> "testId")
    val model: GetBusinessDetailsSuccessResponseModel = GetBusinessDetailsSuccessResponseModel("testId")

    "read from json" in {
      Json.fromJson[GetBusinessDetailsSuccessResponseModel](json) shouldBe JsSuccess(model)
    }
    "write to json" in {
      Json.toJson(model) shouldBe json
    }
  }

  "GetBusinessDetailsFailureResponseModel" should {

    val fullJson: JsObject = Json.obj("code" -> "testCode", "reason" -> "testReason")
    val fullModel: GetBusinessDetailsFailureResponseModel = GetBusinessDetailsFailureResponseModel(Some("testCode"), "testReason")

    val minimalJson: JsObject = Json.obj("reason" -> "testReason")
    val minimalModel: GetBusinessDetailsFailureResponseModel = GetBusinessDetailsFailureResponseModel(None, "testReason")

    "read from json" when {
      "all data is present" in {
        Json.fromJson[GetBusinessDetailsFailureResponseModel](fullJson) shouldBe JsSuccess(fullModel)
      }
      "optional data is not present" in {
        Json.fromJson[GetBusinessDetailsFailureResponseModel](minimalJson) shouldBe JsSuccess(minimalModel)
      }
    }

    "write to json" when {
      "all data is present" in {
        Json.toJson(fullModel) shouldBe fullJson
      }
      "optional data is not present" in {
        Json.toJson(minimalJson) shouldBe minimalJson
      }
    }

  }

}
