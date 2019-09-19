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
import models.subscription.business.Cash
import models.subscription.incomesource.{AccountingPeriod, BusinessIncomeModel, SignUpRequest, PropertyIncomeModel}
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

    val businessIncome = BusinessIncomeModel(
      tradingName = Some(testTradingName),
      accountingPeriod = AccountingPeriod(startDate = DateModel(startDay, startMonth, startYear), endDate = DateModel(endDay, endMonth, endYear)),
      accountingMethod = Cash
    )

    val propertyIncome = PropertyIncomeModel(
      accountingMethod = Some(Cash)
    )

    def incomeSource(testArn: Option[String] = None) = SignUpRequest(testNino, testArn, Some(businessIncome), Some(propertyIncome))

    def testJson(testArn: Option[String] = None) = Json.obj(
      "nino" -> testNino,
      "businessIncome" -> Json.obj(
        "tradingName" -> testTradingName,
        "accountingPeriod" -> Json.obj(
          "startDate" -> Json.obj(
            "day" -> startDay,
            "month" -> startMonth,
            "year" -> startYear
          ),
          "endDate" -> Json.obj(
            "day" -> endDay,
            "month" -> endMonth,
            "year" -> endYear
          )
        ),
        "accountingMethod" -> "cash"
      ),
      "propertyIncome" -> Json.obj(
        "accountingMethod" -> "cash"
      )
    ) ++ testArn.fold(Json.obj())(arn => Json.obj("arn" -> arn))

    "convert to the correct Json" in {
      Json.toJson(incomeSource(Some(testArn))) shouldBe testJson(Some(testArn))
    }

    "convert from json" when {

      "an arn is present" in {
        val result = testJson(Some(testArn)).as[SignUpRequest]

        result shouldBe incomeSource(Some(testArn))
        result.isAgent shouldBe true
      }

      "an arn is not present" in {
        val result = testJson().as[SignUpRequest]

        result shouldBe incomeSource()
        result.isAgent shouldBe false
      }
    }

  }
}
