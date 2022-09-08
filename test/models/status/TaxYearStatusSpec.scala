/*
 * Copyright 2022 HM Revenue & Customs
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

import models.status.MtdMandationStatus.Voluntary
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsError, JsSuccess, Json, __}

class TaxYearStatusSpec extends PlaySpec {
  private val fullModel = TaxYearStatus("2022-23", Voluntary)

  val fullJson = Json.obj(
    "taxYear" -> "2022-23",
    "status" -> "MTD Voluntary"
  )

  "read" must {
    "successfully read from json" when {
      "the json has full details" in {
        Json.fromJson[TaxYearStatus](fullJson) mustBe JsSuccess(fullModel)
      }
    }
    "fail to read" when {
      "taxYear is missing" in {
        Json.fromJson[TaxYearStatus](fullJson - "taxYear") mustBe JsError(__ \ "taxYear", "error.path.missing")
      }
      "status is missing" in {
        Json.fromJson[TaxYearStatus](fullJson - "status") mustBe JsError(__ \ "status", "error.path.missing")
      }
    }
  }

  "write" must {
    "successfully write to json" in {
      Json.toJson(fullModel) mustBe fullJson
    }
  }
}
