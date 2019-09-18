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

package models.subscription.business

import play.api.libs.json._


sealed trait AccountingMethod {
  val stringValue: String
}

case object Cash extends AccountingMethod {
  override val stringValue = "cash"
}

case object Accruals extends AccountingMethod {
  override val stringValue = "accruals"
}

object AccountingMethod {

  private val reader: Reads[AccountingMethod] = __.read[String].map {
    case Cash.stringValue => Cash
    case Accruals.stringValue => Accruals
  }

  private val writer: Writes[AccountingMethod] = Writes[AccountingMethod](cashOrAccruals =>
    JsString(cashOrAccruals.stringValue)
  )

  implicit val format: Format[AccountingMethod] = Format(reader, writer)
}

case class BusinessDetailsModel(accountingPeriodStartDate: String,
                                accountingPeriodEndDate: String,
                                tradingName: String,
                                cashOrAccruals: AccountingMethod
                               )

object BusinessDetailsModel {
  implicit val format: Format[BusinessDetailsModel] = Json.format[BusinessDetailsModel]
}

