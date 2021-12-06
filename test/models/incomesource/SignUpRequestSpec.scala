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

package models.incomesource

import common.CommonSpec
import models.DateModel
import models.subscription.business.{AccountingMethod, Cash}
import models.subscription.incomesource.{AccountingPeriod, BusinessIncomeModel, PropertyIncomeModel}
import play.api.libs.json.Json

class SignUpRequestSpec extends CommonSpec {

  "Creating a model for a subscription request" should {

    val testTradingName = "trader joe"
    val startDay = "06"
    val startMonth = "6"
    val startYear = "2018"
    val endDay = "05"
    val endMonth = "6"
    val endYear = "2019"

    val businessIncome = BusinessIncomeModel(Some(testTradingName),
      AccountingPeriod(startDate = DateModel(startDay, startMonth, startYear), endDate = DateModel(endDay, endMonth, endYear)),
      AccountingMethod.feCash)

    val propertyIncome = PropertyIncomeModel(Cash)

    def businessIncomeJson(testArn: Option[String] = None) = Json.obj(
      "businessDetails" -> Seq(Json.obj(
        "accountingPeriodStartDate" -> "2018-06-06",
        "accountingPeriodEndDate" -> "2019-06-05",
        "tradingName" -> "trader joe",
        "cashOrAccruals" -> "cash"
      ))
    )

    def propertyIncomeJson() = Json.obj("cashAccrualsFlag" -> "C")

    "send the correct json to DES for business income source" in {
      BusinessIncomeModel.writeToDes(businessIncome) shouldBe businessIncomeJson()
    }

    "send the correct json to DES for property income source" in {
      PropertyIncomeModel.writeToDes(propertyIncome) shouldBe propertyIncomeJson()
    }

  }

}
