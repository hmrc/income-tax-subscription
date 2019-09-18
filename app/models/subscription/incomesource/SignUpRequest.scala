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

import models.DateModel
import models.subscription.business.AccountingMethod
import play.api.libs.json.{Json, OFormat}

case class SignUpRequest(nino: String,
                         arn: Option[String],
                         businessIncome: Option[BusinessIncomeModel],
                         propertyIncome: Option[PropertyIncomeModel]
                        ) {

  val isAgent: Boolean = arn.isDefined
}

case class BusinessIncomeModel(tradingName: Option[String],
                               accountingPeriod: AccountingPeriod,
                               accountingMethod: AccountingMethod)

case class PropertyIncomeModel(accountingMethod: Option[AccountingMethod]) //TODO change to non option when cash and accruals has been added to property


case class AccountingPeriod(startDate: DateModel, endDate: DateModel)

object SignUpRequest {
  implicit val format: OFormat[SignUpRequest] = Json.format[SignUpRequest]
}

object BusinessIncomeModel {
  implicit val format: OFormat[BusinessIncomeModel] = Json.format[BusinessIncomeModel]
}

object PropertyIncomeModel {
  implicit val format: OFormat[PropertyIncomeModel] = Json.format[PropertyIncomeModel]
}

object AccountingPeriod {
  implicit val format: OFormat[AccountingPeriod] = Json.format[AccountingPeriod]
}