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

package models.subscription.incomesource

import models.subscription.business.CashOrAccruals
import play.api.libs.json.{Json, OFormat}

case class IncomeSource(nino:String,
                        isAgent:Boolean = false,
                        arn: Option[String],
                        businessIncome: Option[BusinessIncomeModel],
                        propertyIncome: Option[PropertyIncomeModel])

case class BusinessIncomeModel(tradeName: Option[String],
                               accountingPeriod: AccountingPeriod,
                               cashOrAccruals: Option[CashOrAccruals])

case class PropertyIncomeModel(cashOrAccruals: Option[CashOrAccruals])


case class AccountingPeriod(startDate: String, endDate:String)

object IncomeSource {
  implicit val format: OFormat[IncomeSource] = Json.format[IncomeSource]
}

object BusinessIncomeModel {
  implicit val format: OFormat[BusinessIncomeModel] = Json.format[BusinessIncomeModel]
}

object PropertyIncomeModel {
  implicit val format: OFormat[PropertyIncomeModel] = Json.format[PropertyIncomeModel]
}

object AccountingPeriod{
  implicit val format: OFormat[AccountingPeriod] = Json.format[AccountingPeriod]
}
