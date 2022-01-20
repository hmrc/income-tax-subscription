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

package models.subscription

import models.DateModel
import models.subscription.business.{Accruals, Cash}
import org.scalatest.MustMatchers
import org.scalatestplus.play.PlaySpec
import play.api.libs.json._
import uk.gov.hmrc.http.InternalServerException
import utils.TestConstants.testNino

import java.time.LocalDate

class CreateIncomeSourcesModelSpec extends PlaySpec with MustMatchers {

  val mtditid: String = "XAIT0000006"

  val now: LocalDate = LocalDate.now

  val desFormattedNow: String = DateModel.dateConvert(now).toDesDateFormat

  val fullSoleTraderBusinesses: SoleTraderBusinesses = SoleTraderBusinesses(
    accountingPeriod = AccountingPeriodModel(now, now),
    accountingMethod = Cash,
    businesses = Seq(
      SelfEmploymentData(
        id = "testBusinessId",
        businessStartDate = Some(BusinessStartDate(now)),
        businessName = Some(BusinessNameModel("testBusinessName")),
        businessTradeName = Some(BusinessTradeNameModel("testBusinessTrade")),
        businessAddress = Some(BusinessAddressModel(
          auditRef = "testAuditRef",
          address = Address(lines = Seq("line 1", "line 2"), postcode = "testPostcode")
        ))
      )
    )
  )

  val fullUkProperty: UkProperty = UkProperty(
    accountingPeriod = AccountingPeriodModel(now, now),
    tradingStartDate = LocalDate.now,
    accountingMethod = Accruals
  )

  val fullOverseasProperty: OverseasProperty = OverseasProperty(
    accountingPeriod = AccountingPeriodModel(now, now),
    tradingStartDate = LocalDate.now,
    accountingMethod = Cash
  )

  val fullCreateIncomeSourcesModel: CreateIncomeSourcesModel = CreateIncomeSourcesModel(
    nino = testNino,
    soleTraderBusinesses = Some(fullSoleTraderBusinesses),
    ukProperty = Some(fullUkProperty),
    overseasProperty = Some(fullOverseasProperty)
  )

  val fullSoleTraderBusinessesJsonRead: JsObject = Json.obj(
    "accountingPeriod" -> Json.obj(
      "startDate" -> Json.obj(
        "day" -> now.getDayOfMonth.toString,
        "month" -> now.getMonthValue.toString,
        "year" -> now.getYear.toString
      ),
      "endDate" -> Json.obj(
        "day" -> now.getDayOfMonth.toString,
        "month" -> now.getMonthValue.toString,
        "year" -> now.getYear.toString
      )
    ),
    "accountingMethod" -> "Cash",
    "businesses" -> Json.arr(
      Json.obj(
        "id" -> "testBusinessId",
        "businessStartDate" -> Json.obj(
          "startDate" -> Json.obj(
            "day" -> now.getDayOfMonth.toString,
            "month" -> now.getMonthValue.toString,
            "year" -> now.getYear.toString
          )
        ),
        "businessName" -> Json.obj(
          "businessName" -> "testBusinessName"
        ),
        "businessTradeName" -> Json.obj(
          "businessTradeName" -> "testBusinessTrade"
        ),
        "businessAddress" -> Json.obj(
          "auditRef" -> "testAuditRef",
          "address" -> Json.obj(
            "lines" -> Json.arr(
              "line 1",
              "line 2"
            ),
            "postcode" -> "testPostcode"
          )
        )
      )
    )
  )

  val fullUkPropertyJsonRead: JsObject = Json.obj(
    "accountingPeriod" -> Json.obj(
      "startDate" -> Json.obj(
        "day" -> now.getDayOfMonth.toString,
        "month" -> now.getMonthValue.toString,
        "year" -> now.getYear.toString
      ),
      "endDate" -> Json.obj(
        "day" -> now.getDayOfMonth.toString,
        "month" -> now.getMonthValue.toString,
        "year" -> now.getYear.toString
      )
    ),
    "tradingStartDate" -> Json.obj(
      "day" -> now.getDayOfMonth.toString,
      "month" -> now.getMonthValue.toString,
      "year" -> now.getYear.toString
    ),
    "accountingMethod" -> "Accruals"
  )

  val fullOverseasPropertyJsonRead: JsObject = Json.obj(
    "accountingPeriod" -> Json.obj(
      "startDate" -> Json.obj(
        "day" -> now.getDayOfMonth.toString,
        "month" -> now.getMonthValue.toString,
        "year" -> now.getYear.toString
      ),
      "endDate" -> Json.obj(
        "day" -> now.getDayOfMonth.toString,
        "month" -> now.getMonthValue.toString,
        "year" -> now.getYear.toString
      )
    ),
    "tradingStartDate" -> Json.obj(
      "day" -> now.getDayOfMonth.toString,
      "month" -> now.getMonthValue.toString,
      "year" -> now.getYear.toString
    ),
    "accountingMethod" -> "Cash"
  )

  val fullCreateIncomeSourcesModelJsonRead: JsObject = Json.obj(
    "nino" -> testNino,
    "soleTraderBusinesses" -> fullSoleTraderBusinessesJsonRead,
    "ukProperty" -> fullUkPropertyJsonRead,
    "overseasProperty" -> fullOverseasPropertyJsonRead
  )

  val fullCreateIncomeSourcesModelJsonWrite: JsObject = Json.obj(
    "businessDetails" -> Json.arr(
      Json.obj(
        "accountingPeriodStartDate" -> desFormattedNow,
        "accountingPeriodEndDate" -> desFormattedNow,
        "tradingStartDate" -> desFormattedNow,
        "tradingName" -> "testBusinessName",
        "typeOfBusiness" -> "testBusinessTrade",
        "cashOrAccrualsFlag" -> Cash.stringValue.toUpperCase,
        "addressDetails" -> Json.obj(
          "addressLine1" -> "line 1",
          "addressLine2" -> "line 2",
          "postalCode" -> "testPostcode",
          "countryCode" -> "GB"
        )
      )
    ),
    "ukPropertyDetails" -> Json.obj(
      "tradingStartDate" -> desFormattedNow,
      "cashOrAccrualsFlag" -> Accruals.stringValue.toUpperCase,
      "startDate" -> desFormattedNow
    ),
    "foreignPropertyDetails" -> Json.obj(
      "tradingStartDate" -> desFormattedNow,
      "cashOrAccrualsFlag" -> Cash.stringValue.toUpperCase,
      "startDate" -> desFormattedNow
    )
  )

  "CreateIncomeSourcesModel" must {
    "read from json successfully" when {
      "the json is complete and valid" in {
        Json.fromJson[CreateIncomeSourcesModel](fullCreateIncomeSourcesModelJsonRead) mustBe JsSuccess(fullCreateIncomeSourcesModel)
      }
      "the json is missing all optional fields but is valid" in {
        val readModel = Json.fromJson[CreateIncomeSourcesModel](
          fullCreateIncomeSourcesModelJsonRead - "soleTraderBusinesses" - "ukProperty" - "overseasProperty"
        )
        val expectedModel = fullCreateIncomeSourcesModel.copy(soleTraderBusinesses = None, ukProperty = None, overseasProperty = None)

        readModel mustBe JsSuccess(expectedModel)
      }
    }
    "return a read error" when {
      "nino is missing from the json" in {
        Json.fromJson[CreateIncomeSourcesModel](fullCreateIncomeSourcesModelJsonRead - "nino") mustBe JsError(JsPath \ "nino", "error.path.missing")
      }
    }

    "write to json successfully" when {
      "all required fields are present in the model" in {
        Json.toJson(fullCreateIncomeSourcesModel) mustBe fullCreateIncomeSourcesModelJsonWrite
      }
    }
    "return an exception when writing to json" when {
      "addressLine1 is missing in the model" in {
        val missingAddressLineModel = fullCreateIncomeSourcesModel.copy(
          soleTraderBusinesses = Some(fullSoleTraderBusinesses.copy(
            businesses = fullSoleTraderBusinesses.businesses.map { business =>
              business.copy(businessAddress = business.businessAddress.map(_.copy(address = Address(lines = Nil, postcode = "testPostcode"))))
            }
          ))
        )

        intercept[InternalServerException](Json.toJson(missingAddressLineModel))
          .message mustBe "[CreateIncomeSourcesModel] - Unable to create model, addressLine1 is missing"
      }
      "businessName is missing in the model" in {
        val missingBusinessName = fullCreateIncomeSourcesModel.copy(
          soleTraderBusinesses = Some(fullSoleTraderBusinesses.copy(
            businesses = fullSoleTraderBusinesses.businesses.map { business =>
              business.copy(businessName = None)
            }
          ))
        )

        intercept[InternalServerException](Json.toJson(missingBusinessName))
          .message mustBe "[CreateIncomeSourcesModel] - Unable to create model, businessName is missing"
      }
      "tradingName is missing in the model" in {
        val missingTradingName = fullCreateIncomeSourcesModel.copy(
          soleTraderBusinesses = Some(fullSoleTraderBusinesses.copy(
            businesses = fullSoleTraderBusinesses.businesses.map { business =>
              business.copy(businessTradeName = None)
            }
          ))
        )

        intercept[InternalServerException](Json.toJson(missingTradingName))
          .message mustBe "[CreateIncomeSourcesModel] - Unable to create model, tradingName is missing"
      }
      "addressDetails is missing in the model" in {
        val missingAddressDetails = fullCreateIncomeSourcesModel.copy(
          soleTraderBusinesses = Some(fullSoleTraderBusinesses.copy(
            businesses = fullSoleTraderBusinesses.businesses.map { business =>
              business.copy(businessAddress = None)
            }
          ))
        )

        intercept[InternalServerException](Json.toJson(missingAddressDetails))
          .message mustBe "[CreateIncomeSourcesModel] - Unable to create model, addressDetails is missing"
      }
      "tradingStartDate is missing in the model" in {
        val missingTradingStartDate = fullCreateIncomeSourcesModel.copy(
          soleTraderBusinesses = Some(fullSoleTraderBusinesses.copy(
            businesses = fullSoleTraderBusinesses.businesses.map { business =>
              business.copy(businessStartDate = None)
            }
          ))
        )

        intercept[InternalServerException](Json.toJson(missingTradingStartDate))
          .message mustBe "[CreateIncomeSourcesModel] - Unable to create model, tradingStartDate is missing"
      }
    }
  }


  "SoleTraderBusinesses" must {
    "read from json successfully" when {
      "the json is complete and valid" in {
        Json.fromJson[SoleTraderBusinesses](fullSoleTraderBusinessesJsonRead) mustBe JsSuccess(fullSoleTraderBusinesses)
      }
      "the json is missing businesses" in {
        val readModel = Json.fromJson[SoleTraderBusinesses](fullSoleTraderBusinessesJsonRead ++ Json.obj("businesses" -> Json.arr()))
        val expectedModel = fullSoleTraderBusinesses.copy(businesses = Nil)

        readModel mustBe JsSuccess(expectedModel)
      }
    }
    "return a read error" when {
      "accountingPeriod is missing from the json" in {
        Json.fromJson[SoleTraderBusinesses](fullSoleTraderBusinessesJsonRead - "accountingPeriod") mustBe
          JsError(JsPath \ "accountingPeriod", "error.path.missing")
      }
      "accountingMethod is missing from the json" in {
        Json.fromJson[SoleTraderBusinesses](fullSoleTraderBusinessesJsonRead - "accountingMethod") mustBe
          JsError(JsPath \ "accountingMethod", "error.path.missing")
      }
      "businesses is missing from the json" in {
        Json.fromJson[SoleTraderBusinesses](fullSoleTraderBusinessesJsonRead - "businesses") mustBe
          JsError(JsPath \ "businesses", "error.path.missing")
      }
    }
  }

  "UkProperty" must {
    "read the json successfully" when {
      "the json is complete and valid" in {
        Json.fromJson[UkProperty](fullUkPropertyJsonRead) mustBe JsSuccess(fullUkProperty)
      }
    }
    "return a read error" when {
      "accountingPeriod is missing from the json" in {
        Json.fromJson[UkProperty](fullUkPropertyJsonRead - "accountingPeriod") mustBe JsError(JsPath \ "accountingPeriod", "error.path.missing")
      }
      "tradingStartDate is missing from the json" in {
        Json.fromJson[UkProperty](fullUkPropertyJsonRead - "tradingStartDate") mustBe JsError(JsPath \ "tradingStartDate", "error.path.missing")
      }
      "accountingMethod is missing from the json" in {
        Json.fromJson[UkProperty](fullUkPropertyJsonRead - "accountingMethod") mustBe JsError(JsPath \ "accountingMethod", "error.path.missing")
      }
    }
  }

  "OverseasProperty" must {
    "read the json successfully" when {
      "the json is complete and valid" in {
        Json.fromJson[OverseasProperty](fullOverseasPropertyJsonRead) mustBe JsSuccess(fullOverseasProperty)
      }
    }
    "return a read error" when {
      "accountingPeriod is missing from the json" in {
        Json.fromJson[OverseasProperty](fullOverseasPropertyJsonRead - "accountingPeriod") mustBe JsError(JsPath \ "accountingPeriod", "error.path.missing")
      }
      "tradingStartDate is missing from the json" in {
        Json.fromJson[OverseasProperty](fullOverseasPropertyJsonRead - "tradingStartDate") mustBe JsError(JsPath \ "tradingStartDate", "error.path.missing")
      }
      "accountingMethod is missing from the json" in {
        Json.fromJson[OverseasProperty](fullOverseasPropertyJsonRead - "accountingMethod") mustBe JsError(JsPath \ "accountingMethod", "error.path.missing")
      }
    }
  }

}
