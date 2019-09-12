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

import models.subscription.business.{Cash, CashOrAccruals}
import models.subscription.incomesource.{AccountingPeriod, BusinessIncomeModel, IncomeSource, PropertyIncomeModel}
import uk.gov.hmrc.play.test.UnitSpec

class IncomeSourceSpec extends UnitSpec {
  "Creating a model for a subscription request" should {
    val businessIncome = BusinessIncomeModel(Some("trader joe"), AccountingPeriod(startDate = "06-6-2018", endDate = "05-6-2019"), Some(CashOrAccruals.feCash))
    val incomeSource = IncomeSource("nino", false, Some("arn"), Some(businessIncome), Some(PropertyIncomeModel(cashOrAccruals = Some(CashOrAccruals.feCash ))))

    "display top level data" in {
      incomeSource.nino shouldBe "nino"
      incomeSource.isAgent shouldBe false
      incomeSource.arn.get shouldBe "arn"
    }

    "display business income source" in {
      incomeSource.businessIncome.get.tradeName.get shouldBe "trader joe"
      incomeSource.businessIncome.get.accountingPeriod.startDate shouldBe "06-6-2018"
      incomeSource.businessIncome.get.accountingPeriod.endDate shouldBe "05-6-2019"
      incomeSource.businessIncome.get.cashOrAccruals.get shouldBe Cash
    }

    "displace property income source" in {
      incomeSource.propertyIncome.get.cashOrAccruals.get shouldBe Cash
    }

  }
}
