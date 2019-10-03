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

package models.incomesource

import models.DateModel
import models.subscription.business.{AccountingMethod, Cash}
import models.subscription.incomesource.{AccountingPeriod, BusinessIncomeModel, PropertyIncomeModel, SignUpRequest}
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class SignUpRequestSpec extends UnitSpec {


  "Creating a model for a subscription request" should {

    val testNino = "nino"
    val testArn = "arn"
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

    val propertyIncome = PropertyIncomeModel(Some(Cash))
    val propertyNoIncome = PropertyIncomeModel(None)

    def businessIncomeJson(testArn: Option[String] = None) = Json.obj(
      "businessDetails" ->  Seq(Json.obj(
        "accountingPeriodStartDate" -> "2018-06-06",
        "accountingPeriodEndDate" -> "2019-06-05",
        "tradingName" -> "trader joe",
        "cashOrAccruals" -> "cash"
      ))
    )

    def propertyIncomeJson() = Json.obj("cashAccrualsFlag" -> "C")
    def propertyNoIncomeJson() = Json.obj()

    "send the correct json to DES for business income source" in {

      val result = BusinessIncomeModel.writeToDes(businessIncome) shouldBe businessIncomeJson()

    }

    "send the correct json to DES for property income source" in {
      val result = PropertyIncomeModel.writeToDes(propertyIncome) shouldBe propertyIncomeJson()
    }

    "send the correct json to DES for property income source with no cash or accurals" in {
      val result = PropertyIncomeModel.writeToDes(propertyNoIncome) shouldBe propertyNoIncomeJson()
    }
  }

}
