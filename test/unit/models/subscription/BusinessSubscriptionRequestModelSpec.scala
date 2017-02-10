/*
 * Copyright 2017 HM Revenue & Customs
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

package unit.models.subscription

import models.subscription.business.{BusinessDetailsModel, BusinessSubscriptionRequestModel}
import uk.gov.hmrc.play.test.UnitSpec

class BusinessSubscriptionRequestModelSpec extends UnitSpec {

  "Creating a model for a subscription request" should {
    val businessDetailsModel = BusinessDetailsModel(
      accountingPeriodStartDate = "2017-04-01",
      accountingPeriodEndDate = "2018-03-30",
      tradingName = "Test Business",
      cashOrAccruals = "cash"
    )
    val model = BusinessSubscriptionRequestModel(businessDetailsModel)

    "Accounting Period Start Date should be '2017-04-01'" in {
      model.businessDetails.accountingPeriodStartDate shouldBe "2017-04-01"
    }

    "Accounting Period End Date should be '2018-03-30'" in {
      model.businessDetails.accountingPeriodEndDate shouldBe "2018-03-30"
    }

    "Trading Name should be 'Test Business'" in {
      model.businessDetails.tradingName shouldBe "Test Business"
    }

    "Cash or Accruals should be 'cash'" in {
      model.businessDetails.cashOrAccruals shouldBe "cash"
    }
  }

}
