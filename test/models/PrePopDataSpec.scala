/*
 * Copyright 2024 HM Revenue & Customs
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

package models

import models.subscription.Address
import models.subscription.business.{Accruals, Cash}
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.PlaySpec
import play.api.libs.json._

class PrePopDataSpec extends PlaySpec with Matchers {

  "PrePopSelfEmployment" should {
    "successfully read from json" when {
      "all data is present" in {
        Json.fromJson[PrePopSelfEmployment](selfEmploymentJsonFull) mustBe JsSuccess(selfEmploymentModelFull)
      }
      "all optional data is missing" in {
        Json.fromJson[PrePopSelfEmployment](selfEmploymentJsonMinimal) mustBe JsSuccess(selfEmploymentModelMinimal)
      }
      "trade is longer than 35 characters" in {
        Json.fromJson[PrePopSelfEmployment](Json.obj(
          "businessName" -> "AB 123",
          "businessDescription" -> ("A" * 36),
          "accountingMethod" -> "A"
        )) mustBe JsSuccess(PrePopSelfEmployment(
          name = "AB 123",
          trade = None,
          address = None,
          startDate = None,
          accountingMethod = Accruals
        ))
      }
      "trade does not contain a minimum of 2 letters" in {
        Json.fromJson[PrePopSelfEmployment](Json.obj(
          "businessName" -> "AB 123",
          "businessDescription" -> "P 383",
          "accountingMethod" -> "A"
        )) mustBe JsSuccess(PrePopSelfEmployment(
          name = "AB 123",
          trade = None,
          address = None,
          startDate = None,
          accountingMethod = Accruals
        ))
      }
      "trade contains characters which are not allowed" in {
        Json.fromJson[PrePopSelfEmployment](Json.obj(
          "businessName" -> "AB 123",
          "businessDescription" -> """!@£$%^*()_+}{":?><~`#§± AZaz09&'/\.,-""",
          "accountingMethod" -> "A"
        )) mustBe JsSuccess(PrePopSelfEmployment(
          name = "AB 123",
          trade = Some("""AZaz09&'/\.,-"""),
          address = None,
          startDate = None,
          accountingMethod = Accruals
        ))
      }
      "name contains characters which are not allowed" in {
        Json.fromJson[PrePopSelfEmployment](Json.obj(
          "businessName" -> """!@£$%^*()_+}{":?><~`#§± AZaz09&'/\.,-""",
          "businessDescription" -> "Plumbing",
          "accountingMethod" -> "A"
        )) mustBe JsSuccess(PrePopSelfEmployment(
          name = """AZaz09&'/\.,-""",
          trade = Some("Plumbing"),
          address = None,
          startDate = None,
          accountingMethod = Accruals
        ))
      }
    }
    "fail to read from json" when {
      "business name is missing" in {
        Json.fromJson[PrePopSelfEmployment](selfEmploymentJsonFull - "businessName") mustBe JsError(__ \ "businessName", "error.path.missing")
      }
      "business description is missing" in {
        Json.fromJson[PrePopSelfEmployment](selfEmploymentJsonFull - "businessDescription") mustBe JsError(__ \ "businessDescription", "error.path.missing")
      }
      "accounting method is missing" in {
        Json.fromJson[PrePopSelfEmployment](selfEmploymentJsonFull - "accountingMethod") mustBe JsError(__ \ "accountingMethod", "error.path.missing")
      }
    }

    "successfully write to json" when {
      "all optional data items are present" in {
        Json.toJson(selfEmploymentModelFull) mustBe selfEmploymentJsonWriteFull
      }
      "all optional data items are not present" in {
        Json.toJson(selfEmploymentModelMinimal) mustBe selfEmploymentJsonWriteMinimal
      }
    }
  }

  lazy val selfEmploymentJsonFull: JsObject = Json.obj(
    "businessName" -> "AB 123",
    "businessDescription" -> "EL 987",
    "businessAddressFirstLine" -> "1 long road",
    "businessAddressPostcode" -> "ZZ1 1ZZ",
    "dateBusinessStarted" -> "1900-01-01",
    "accountingMethod" -> "A"
  )
  lazy val selfEmploymentJsonWriteFull: JsObject = Json.obj(
    "name" -> "AB 123",
    "trade" -> "EL 987",
    "address" -> Json.obj(
      "lines" -> Json.arr(
        "1 long road"
      ),
      "postcode" -> "ZZ1 1ZZ"
    ),
    "startDate" -> Json.obj(
      "day" -> "01",
      "month" -> "01",
      "year" -> "1900"
    ),
    "accountingMethod" -> Accruals.stringValue
  )
  lazy val selfEmploymentModelFull: PrePopSelfEmployment = PrePopSelfEmployment(
    name = "AB 123",
    trade = Some("EL 987"),
    address = Some(Address(
      lines = Seq("1 long road"),
      postcode = Some("ZZ1 1ZZ")
    )),
    startDate = Some(DateModel("01", "01", "1900")),
    accountingMethod = Accruals
  )
  lazy val selfEmploymentJsonMinimal: JsObject = Json.obj(
    "businessName" -> "AB 123",
    "businessDescription" -> "PL 567",
    "accountingMethod" -> "C"
  )
  lazy val selfEmploymentJsonWriteMinimal: JsObject = Json.obj(
    "name" -> "AB 123",
    "trade" -> "PL 567",
    "accountingMethod" -> Cash.stringValue
  )
  lazy val selfEmploymentModelMinimal: PrePopSelfEmployment = PrePopSelfEmployment(
    name = "AB 123",
    trade = Some("PL 567"),
    address = None,
    startDate = None,
    accountingMethod = Cash
  )

  "PrePopData" should {
    "successfully read from json" when {
      "all data is present" in {
        Json.fromJson[PrePopData](prePopDataJsonFull) mustBe JsSuccess(prePopDataModelFull)
      }
      "all optional data is missing" in {
        Json.fromJson[PrePopData](prePopDataJsonMinimal) mustBe JsSuccess(prePopDataModelMinimal)
      }
      "uk property and foreign property are present but their inner accounting method is missing" in {
        Json.fromJson[PrePopData](Json.obj(
          "ukProperty" -> Json.obj(),
          "foreignProperty" -> Json.arr()
        )) mustBe JsSuccess(prePopDataModelMinimal)
      }
    }
    "successfully write to json" when {
      "all optional data values are present" in {
        Json.toJson(prePopDataModelFull) mustBe Json.obj(
          "selfEmployment" -> Json.arr(
            selfEmploymentJsonWriteFull,
            selfEmploymentJsonWriteMinimal
          ),
          "ukPropertyAccountingMethod" -> Accruals.stringValue,
          "foreignPropertyAccountingMethod" -> Cash.stringValue
        )
      }
      "all optional data values are missing" in {
        Json.toJson(prePopDataModelMinimal) mustBe Json.obj()
      }
    }
  }

  lazy val prePopDataJsonFull: JsObject = Json.obj(
    "selfEmployment" -> Json.arr(
      selfEmploymentJsonFull,
      selfEmploymentJsonMinimal
    ),
    "ukProperty" -> Json.obj(
      "accountingMethod" -> "A"
    ),
    "foreignProperty" -> Json.arr(
      Json.obj(
        "accountingMethod" -> "C"
      ),
      Json.obj(
        "accountingMethod" -> "A"
      )
    )
  )
  lazy val prePopDataModelFull: PrePopData = PrePopData(
    selfEmployment = Some(Seq(
      selfEmploymentModelFull,
      selfEmploymentModelMinimal
    )),
    ukPropertyAccountingMethod = Some(Accruals),
    foreignPropertyAccountingMethod = Some(Cash)
  )

  lazy val prePopDataJsonMinimal: JsObject = Json.obj()
  lazy val prePopDataModelMinimal: PrePopData = PrePopData(None, None, None)

}
