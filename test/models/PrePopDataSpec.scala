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
        )) mustBe JsSuccess(PrePopSelfEmployment(
          name = Some("AB 123"),
          trade = None,
          address = None,
          startDate = None
        ))
      }
      "trade does not contain a minimum of 2 letters" in {
        Json.fromJson[PrePopSelfEmployment](Json.obj(
          "businessName" -> "AB 123",
          "businessDescription" -> "P 383"
        )) mustBe JsSuccess(PrePopSelfEmployment(
          name = Some("AB 123"),
          trade = None,
          address = None,
          startDate = None
        ))
      }
      "trade contains characters which are not allowed" in {
        Json.fromJson[PrePopSelfEmployment](Json.obj(
          "businessName" -> "AB 123",
          "businessDescription" -> """!@£$%^*()_+}{":?><~`#§± AZaz09&'/\.,-"""
        )) mustBe JsSuccess(PrePopSelfEmployment(
          name = Some("AB 123"),
          trade = Some("""AZaz09&'/\.,-"""),
          address = None,
          startDate = None
        ))
      }
      "name contains characters which are not allowed" in {
        Json.fromJson[PrePopSelfEmployment](Json.obj(
          "businessName" -> """!@£$%^*()_+}{":?><~`#§± AZaz09&'/\.,-""",
          "businessDescription" -> "Plumbing"
        )) mustBe JsSuccess(PrePopSelfEmployment(
          name = Some("""AZaz09&'/\.,-"""),
          trade = Some("Plumbing"),
          address = None,
          startDate = None
        ))
      }
      "incomplete address with only the first line provided but no postcode" in {
        Json.fromJson[PrePopSelfEmployment](Json.obj(
          "businessName" -> "AB 123",
          "businessDescription" -> "Plumbing",
          "businessAddressFirstLine" -> "1 long road"
        )) mustBe JsSuccess(PrePopSelfEmployment(
          name = Some("AB 123"),
          trade = Some("Plumbing"),
          address = None,
          startDate = None
        ))
      }
      "a Valid Postcode contains characters which are not allowed and lowercase letters" in {
        Json.fromJson[PrePopSelfEmployment](Json.obj(
          "businessName" -> "AB 123",
          "businessDescription" -> "Plumbing",
          "businessAddressFirstLine" -> "1 long road",
          "businessAddressPostcode" -> """$%*&^£#!-_ZZ1%^ 1zz""",
        )) mustBe JsSuccess(PrePopSelfEmployment(
          name = Some("AB 123"),
          trade = Some("Plumbing"),
          address = Some(Address(
            lines = Seq("1 long road"),
            postcode = Some("ZZ1 1ZZ")
          )),
          startDate = None
        ))
      }
      "a postcode in invalid format" in {
        Json.fromJson[PrePopSelfEmployment](Json.obj(
          "businessName" -> "AB 123",
          "businessDescription" -> "Plumbing",
          "businessAddressFirstLine" -> "1 long road",
          "businessAddressPostcode" -> "ZZ1 1Z Z",
        )) mustBe JsSuccess(PrePopSelfEmployment(
          name = Some("AB 123"),
          trade = Some("Plumbing"),
          address = None,
          startDate = None
        ))
      }
    }
    "fail to read from json" when {
      "business description is missing" in {
        Json.fromJson[PrePopSelfEmployment](selfEmploymentJsonFull - "businessDescription") mustBe JsError(__ \ "businessDescription", "error.path.missing")
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
    "dateBusinessStarted" -> "1900-01-01"
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
    )
  )
  lazy val selfEmploymentModelFull: PrePopSelfEmployment = PrePopSelfEmployment(
    name = Some("AB 123"),
    trade = Some("EL 987"),
    address = Some(Address(
      lines = Seq("1 long road"),
      postcode = Some("ZZ1 1ZZ")
    )),
    startDate = Some(DateModel("01", "01", "1900"))
  )
  lazy val selfEmploymentJsonMinimal: JsObject = Json.obj(
    "businessDescription" -> "PL 567"
  )
  lazy val selfEmploymentJsonWriteMinimal: JsObject = Json.obj(
    "trade" -> "PL 567"
  )
  lazy val selfEmploymentModelMinimal: PrePopSelfEmployment = PrePopSelfEmployment(
    name = None,
    trade = Some("PL 567"),
    address = None,
    startDate = None
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
          )
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
    )
  )
  lazy val prePopDataModelFull: PrePopData = PrePopData(
    selfEmployment = Some(Seq(
      selfEmploymentModelFull,
      selfEmploymentModelMinimal
    ))
  )

  lazy val prePopDataJsonMinimal: JsObject = Json.obj()
  lazy val prePopDataModelMinimal: PrePopData = PrePopData(None)

}
