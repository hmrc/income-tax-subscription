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

package models.status

import models.status.ITSAStatus.{MTDMandated, MTDVoluntary}
import org.scalatestplus.play.PlaySpec
import play.api.libs.json._

class MandationStatusResponseSpec extends PlaySpec {

  val fullModel: MandationStatusResponse = MandationStatusResponse(
    currentYearStatus = MTDVoluntary,
    nextYearStatus = MTDMandated
  )

  val fullJson: JsObject = Json.obj(
    "currentYearStatus" -> "MTD Voluntary",
    "nextYearStatus" -> "MTD Mandated"
  )

  "read" must {
    "successfully read from json" when {
      "the json has full details" in {
        Json.fromJson[MandationStatusResponse](fullJson) mustBe JsSuccess(fullModel)
      }
    }
    "fail to read" when {
      "currentYearStatus is missing" in {
        Json.fromJson[MandationStatusResponse](fullJson - "currentYearStatus") mustBe JsError(__ \ "currentYearStatus", "error.path.missing")
      }
      "nextYearStatus is missing" in {
        Json.fromJson[MandationStatusResponse](fullJson - "nextYearStatus") mustBe JsError(__ \ "nextYearStatus", "error.path.missing")
      }
    }
  }

  "write" must {
    "successfully write to json" in {
      Json.toJson(fullModel) mustBe fullJson
    }
  }

}
