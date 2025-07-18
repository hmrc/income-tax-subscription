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

package models.subscription

import models.DateModel
import models.subscription.business.{Accruals, Cash}
import org.scalatestplus.play.PlaySpec
import play.api.libs.json._
import uk.gov.hmrc.http.InternalServerException
import utils.TestConstants.testNino

import java.time.LocalDate

class CreateIncomeSourcesModelSpec extends PlaySpec {

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
      "startDateBeforeLimit is false and all fields are present in the model" in {
        Json.toJson(fullCreateIncomeSourcesModel)(CreateIncomeSourcesModel.hipWrites(mtditid)) mustBe fullModelTradingStartDateJsonWrite
      }
      "startDateBeforeLimit is true and all fields are present in the model" in {
        lazy val fullModelWithContextualTaxYear = CreateIncomeSourcesModel(
          nino = testNino,
          soleTraderBusinesses = Some(fullSoleTraderBusinesses.copy(businesses = Seq(testSelfEmploymentData.copy(startDateBeforeLimit = true)))),
          ukProperty = Some(fullUkProperty.copy(startDateBeforeLimit = true)),
          overseasProperty = Some(fullOverseasProperty.copy(startDateBeforeLimit = true))
        )
        Json.toJson(fullModelWithContextualTaxYear)(CreateIncomeSourcesModel.hipWrites(mtditid)) mustBe fullModelContextualTaxYearJsonWrite
      }
      "accounting method is not present" in {
        lazy val fullModelNoAccountingMethod = CreateIncomeSourcesModel(
          nino = testNino,
          soleTraderBusinesses = Some(fullSoleTraderBusinesses.copy(
            accountingMethod = None,
            businesses = Seq(testSelfEmploymentData.copy(startDateBeforeLimit = true)))),
          ukProperty = Some(fullUkProperty.copy(startDateBeforeLimit = true, accountingMethod = None)),
          overseasProperty = Some(fullOverseasProperty.copy(startDateBeforeLimit = true, accountingMethod = None))
        )
        Json.toJson(fullModelNoAccountingMethod)(CreateIncomeSourcesModel.hipWrites(mtditid)) mustBe fullModelNoAccountingMethodJsonWrite
      }
    }
    "return an exception when writing to json" when {
      "addressLine1 is missing in the model" in {
        val missingAddressLineModel = fullCreateIncomeSourcesModel.copy(
          soleTraderBusinesses = Some(fullSoleTraderBusinesses.copy(
            businesses = fullSoleTraderBusinesses.businesses.map { business =>
              business.copy(businessAddress = business.businessAddress.map(_.copy(address = Address(lines = Nil, postcode = Some("testPostcode")))))
            }
          ))
        )

        intercept[InternalServerException](Json.toJson(missingAddressLineModel)(CreateIncomeSourcesModel.hipWrites(mtditid)))
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

        intercept[InternalServerException](Json.toJson(missingBusinessName)(CreateIncomeSourcesModel.hipWrites(mtditid)))
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

        intercept[InternalServerException](Json.toJson(missingTradingName)(CreateIncomeSourcesModel.hipWrites(mtditid)))
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

        intercept[InternalServerException](Json.toJson(missingAddressDetails)(CreateIncomeSourcesModel.hipWrites(mtditid)))
          .message mustBe "[CreateIncomeSourcesModel] - Unable to create model, address is missing"
      }
      "tradingStartDate is missing in the model" in {
        val missingTradingStartDate = fullCreateIncomeSourcesModel.copy(
          soleTraderBusinesses = Some(fullSoleTraderBusinesses.copy(
            businesses = fullSoleTraderBusinesses.businesses.map { business =>
              business.copy(businessStartDate = None)
            }
          ))
        )

        intercept[InternalServerException](Json.toJson(missingTradingStartDate)(CreateIncomeSourcesModel.hipWrites(mtditid)))
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
    }
  }

  lazy val mtditid: String = "XAIT0000006"

  lazy val now: LocalDate = LocalDate.now

  lazy val desFormattedNow: String = DateModel.dateConvert(now).toDesDateFormat

  lazy val testSelfEmploymentData: SelfEmploymentData = SelfEmploymentData(
    id = "testBusinessId",
    businessStartDate = Some(BusinessStartDate(now)),
    businessName = Some(BusinessNameModel("testBusinessName")),
    businessTradeName = Some(BusinessTradeNameModel("testBusinessTrade")),
    businessAddress = Some(BusinessAddressModel(
      address = Address(lines = Seq("line 1", "line 2"), postcode = Some("testPostcode"))
    )),
    startDateBeforeLimit = false
  )

  lazy val fullSoleTraderBusinesses: SoleTraderBusinesses = SoleTraderBusinesses(
    accountingPeriod = AccountingPeriodModel(now, now),
    accountingMethod = Some(Cash),
    businesses = Seq(
      testSelfEmploymentData
    )
  )

  lazy val fullUkProperty: UkProperty = UkProperty(
    accountingPeriod = AccountingPeriodModel(now, now),
    startDateBeforeLimit = false,
    tradingStartDate = LocalDate.now,
    accountingMethod = Some(Accruals)
  )

  lazy val fullOverseasProperty: OverseasProperty = OverseasProperty(
    accountingPeriod = AccountingPeriodModel(now, now),
    startDateBeforeLimit = false,
    tradingStartDate = LocalDate.now,
    accountingMethod = Some(Cash)
  )

  lazy val fullCreateIncomeSourcesModel: CreateIncomeSourcesModel = CreateIncomeSourcesModel(
    nino = testNino,
    soleTraderBusinesses = Some(fullSoleTraderBusinesses),
    ukProperty = Some(fullUkProperty),
    overseasProperty = Some(fullOverseasProperty)
  )

  lazy val fullSoleTraderBusinessesJsonRead: JsObject = Json.obj(
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
          "address" -> Json.obj(
            "lines" -> Json.arr(
              "line 1",
              "line 2"
            ),
            "postcode" -> "testPostcode"
          )
        ),
        "startDateBeforeLimit" -> false
      )
    )
  )

  lazy val ukPropertyNoAccountingMethodJsonRead: JsObject = Json.obj(
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
    "startDateBeforeLimit" -> false,
    "tradingStartDate" -> Json.obj(
      "day" -> now.getDayOfMonth.toString,
      "month" -> now.getMonthValue.toString,
      "year" -> now.getYear.toString
    )
  )

  lazy val fullUkPropertyJsonRead: JsObject = Json.obj(
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
    "startDateBeforeLimit" -> false,
    "tradingStartDate" -> Json.obj(
      "day" -> now.getDayOfMonth.toString,
      "month" -> now.getMonthValue.toString,
      "year" -> now.getYear.toString
    ),
    "accountingMethod" -> "Accruals"
  )

  lazy val fullOverseasPropertyJsonRead: JsObject = Json.obj(
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
    "startDateBeforeLimit" -> false,
    "accountingMethod" -> "Cash"
  )

  lazy val fullCreateIncomeSourcesModelJsonRead: JsObject = Json.obj(
    "nino" -> testNino,
    "soleTraderBusinesses" -> fullSoleTraderBusinessesJsonRead,
    "ukProperty" -> fullUkPropertyJsonRead,
    "overseasProperty" -> fullOverseasPropertyJsonRead
  )

  lazy val fullCreateIncomeSourcesModelJsonWrite: JsObject = Json.obj(
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

  lazy val fullCreateIncomeSourcesModelJsonWriteMinimal: JsObject = Json.obj(
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

  lazy val fullModelTradingStartDateJsonWrite: JsObject = Json.obj(
    "mtdbsa" -> mtditid,
    "businessDetails" -> Json.arr(
      Json.obj(
        "accountingPeriodStartDate" -> desFormattedNow,
        "accountingPeriodEndDate" -> desFormattedNow,
        "tradingName" -> "testBusinessName",
        "typeOfBusiness" -> "testBusinessTrade",
        "cashAccrualsFlag" -> Cash.stringValue.take(1).toUpperCase,
        "address" -> Json.obj(
          "addressLine1" -> "line 1",
          "countryCode" -> "GB",
          "postcode" -> "testPostcode",
          "addressLine2" -> "line 2"
        ),
        "tradingStartDate" -> desFormattedNow
      )
    ),
    "ukPropertyDetails" -> Json.obj(

      "cashAccrualsFlag" -> Accruals.stringValue.take(1).toUpperCase,
      "startDate" -> desFormattedNow,
      "tradingStartDate" -> desFormattedNow
    ),
    "foreignPropertyDetails" -> Json.obj(

      "cashAccrualsFlag" -> Cash.stringValue.take(1).toUpperCase,
      "startDate" -> desFormattedNow,
      "tradingStartDate" -> desFormattedNow

    )
  )

  lazy val contextualTaxYear: String = (DateModel.dateConvert(now).getYear + 1).toString
  lazy val fullModelContextualTaxYearJsonWrite: JsObject = Json.obj(
    "mtdbsa" -> mtditid,
    "businessDetails" -> Json.arr(
      Json.obj(
        "accountingPeriodStartDate" -> desFormattedNow,
        "accountingPeriodEndDate" -> desFormattedNow,
        "tradingName" -> "testBusinessName",
        "typeOfBusiness" -> "testBusinessTrade",
        "cashAccrualsFlag" -> Cash.stringValue.take(1).toUpperCase,
        "address" -> Json.obj(
          "addressLine1" -> "line 1",
          "countryCode" -> "GB",
          "postcode" -> "testPostcode",
          "addressLine2" -> "line 2"
        ),
        "contextualTaxYear" -> contextualTaxYear
      )
    ),
    "ukPropertyDetails" -> Json.obj(
      "cashAccrualsFlag" -> Accruals.stringValue.take(1).toUpperCase,
      "startDate" -> desFormattedNow,
      "contextualTaxYear" -> contextualTaxYear
    ),
    "foreignPropertyDetails" -> Json.obj(
      "cashAccrualsFlag" -> Cash.stringValue.take(1).toUpperCase,
      "startDate" -> desFormattedNow,
      "contextualTaxYear" -> contextualTaxYear

    )
  )
  lazy val fullModelNoAccountingMethodJsonWrite: JsObject = Json.obj(
    "mtdbsa" -> mtditid,
    "businessDetails" -> Json.arr(
      Json.obj(
        "accountingPeriodStartDate" -> desFormattedNow,
        "accountingPeriodEndDate" -> desFormattedNow,
        "tradingName" -> "testBusinessName",
        "typeOfBusiness" -> "testBusinessTrade",
        "address" -> Json.obj(
          "addressLine1" -> "line 1",
          "countryCode" -> "GB",
          "postcode" -> "testPostcode",
          "addressLine2" -> "line 2"
        ),
        "contextualTaxYear" -> contextualTaxYear
      )
    ),
    "ukPropertyDetails" -> Json.obj(
      "startDate" -> desFormattedNow,
      "contextualTaxYear" -> contextualTaxYear
    ),
    "foreignPropertyDetails" -> Json.obj(
      "startDate" -> desFormattedNow,
      "contextualTaxYear" -> contextualTaxYear

    )
  )

}
