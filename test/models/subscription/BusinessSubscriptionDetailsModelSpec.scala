/*
 * Copyright 2020 HM Revenue & Customs
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
import org.scalatest.MustMatchers.convertToAnyMustWrapper
import play.api.libs.json.{JsNull, Json}
import uk.gov.hmrc.play.test.UnitSpec

class BusinessSubscriptionDetailsModelSpec extends UnitSpec {

  "Creating a model for a subscription request with all values" should {
    val businessDetailsModel = BusinessSubscriptionDetailsModel(
      accountingPeriod = AccountingPeriodModel(DateModel("6", "4", "2018"), DateModel("5", "4", "2019")),
      selfEmploymentsData = Seq(SelfEmploymentData(
        id = "id1",
        businessStartDate = Some(BusinessStartDate(DateModel("1", "1", "2017"))),
        businessName = Some(BusinessNameModel("ABC Limited")),
        businessTradeName = Some(BusinessTradeNameModel("Plumbing")),
        businessAddress = Some(BusinessAddressModel("12345", Address(Seq("line1", "line2", "line3", "line4"), "TF3 4NT")))
      )),
      accountingMethod = Cash,
      incomeSource = FeIncomeSourceModel(true, true, true),
      propertyCommencementDate = Some(PropertyCommencementDateModel(DateModel("6", "7", "2018"))),
      propertyAccountingMethod = Some(AccountingMethodPropertyModel(Accruals)),
      overseasPropertyCommencementDate = Some(OverseasPropertyCommencementDateModel(DateModel("6", "8", "2018"))),
      overseasAccountingMethodProperty = Some(OverseasAccountingMethodPropertyModel(Cash))
    )

    val json = Json.obj("businessDetails" -> Json.arr(
      Json.obj("accountingPeriodStartDate" -> "2018-04-06", "cashOrAccrualsFlag" -> "CASH", "typeOfBusiness" -> "self-employment",
        "addressDetails" -> Json.obj("addressLine1" -> "line1", "addressLine2" ->"line2", "addressLine3" -> "line3", "addressLine4" -> "line4",
          "postalCode" -> "TF3 4NT", "countryCode" -> "GB"),
        "tradingName" -> "Plumbing", "tradingStartDate" -> "2017-01-01", "accountingPeriodEndDate" -> "2019-04-05")),
      "ukPropertyDetails" -> Json.obj("tradingStartDate" -> "2018-07-06","cashOrAccrualsFlag" -> "ACCRUALS",
        "startDate" -> "2018-04-06"),
      "foreignPropertyDetails" -> Json.obj("tradingStartDate" -> "2018-08-06", "cashOrAccrualsFlag" -> "CASH", "startDate" -> "2018-04-06"))


    "write from Json correctly" in {
      Json.toJson(businessDetailsModel) mustBe json
    }
  }

  "Creating a model for a subscription request with all values except foreign property and address line4" should {
    val businessDetailsModel = BusinessSubscriptionDetailsModel(
      accountingPeriod = AccountingPeriodModel(DateModel("6", "4", "2018"), DateModel("5", "4", "2019")),
      selfEmploymentsData = Seq(SelfEmploymentData(
        id = "id1",
        businessStartDate = Some(BusinessStartDate(DateModel("1", "1", "2017"))),
        businessName = Some(BusinessNameModel("ABC Limited")),
        businessTradeName = Some(BusinessTradeNameModel("Plumbing")),
        businessAddress = Some(BusinessAddressModel("12345", Address(Seq("line1", "line2", "line3"), "TF3 4NT")))
      )),
      accountingMethod = Cash,
      incomeSource = FeIncomeSourceModel(true, true, false),
      propertyCommencementDate = Some(PropertyCommencementDateModel(DateModel("6", "7", "2018"))),
      propertyAccountingMethod = Some(AccountingMethodPropertyModel(Accruals))
    )

    val json = Json.obj("businessDetails" -> Json.arr(
      Json.obj("accountingPeriodStartDate" -> "2018-04-06", "cashOrAccrualsFlag" -> "CASH", "typeOfBusiness" -> "self-employment",
        "addressDetails" -> Json.obj("addressLine1" -> "line1", "addressLine2" ->"line2", "addressLine3" -> "line3",
          "postalCode" -> "TF3 4NT", "countryCode" -> "GB"),
        "tradingName" -> "Plumbing", "tradingStartDate" -> "2017-01-01", "accountingPeriodEndDate" -> "2019-04-05")),
      "ukPropertyDetails" -> Json.obj("tradingStartDate" -> "2018-07-06","cashOrAccrualsFlag" -> "ACCRUALS",
        "startDate" -> "2018-04-06"))


    "write from Json correctly" in {
      Json.toJson(businessDetailsModel) mustBe json
    }
  }

  "Creating a model for a subscription request with mandatory fields missing" should {
    val businessDetailsModel = BusinessSubscriptionDetailsModel(
      accountingPeriod = AccountingPeriodModel(DateModel("6", "4", "2018"), DateModel("5", "4", "2019")),
      selfEmploymentsData = Seq(SelfEmploymentData(
        id = "id1",
        businessStartDate = None,
        businessName = Some(BusinessNameModel("ABC Limited")),
        businessTradeName = Some(BusinessTradeNameModel("Plumbing")),
        businessAddress = Some(BusinessAddressModel("12345", Address(Seq("line1", "line2", "line3"), "TF3 4NT")))
      )),
      accountingMethod = Cash,
      incomeSource = FeIncomeSourceModel(true, true, false),
      propertyCommencementDate = Some(PropertyCommencementDateModel(DateModel("6", "7", "2018"))),
      propertyAccountingMethod = Some(AccountingMethodPropertyModel(Accruals))
    )

    val json = Json.obj("businessDetails" -> Json.arr(
      Json.obj("accountingPeriodStartDate" -> "2018-04-06", "cashOrAccrualsFlag" -> "CASH", "typeOfBusiness" -> "self-employment",
        "addressDetails" -> Json.obj("addressLine1" -> "line1", "addressLine2" ->"line2", "addressLine3" -> "line3",
          "postalCode" -> "TF3 4NT", "countryCode" -> "GB"),
        "tradingName" -> "Plumbing", "tradingStartDate" -> "2017-01-01", "accountingPeriodEndDate" -> "2019-04-05")),
      "ukPropertyDetails" -> Json.obj("tradingStartDate" -> "2018-07-06","cashOrAccrualsFlag" -> "ACCRUALS",
        "startDate" -> "2018-04-06"))


    "should throw exception" in {
      intercept[Exception](Json.toJson(businessDetailsModel)).getMessage mustBe "Missing businessStartDate Parameter"
    }
  }
}
