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


sealed trait CashOrAccruals {
  def cashOrAccruals: String
}

case object Cash extends CashOrAccruals {
  override val cashOrAccruals = "cash"
}

case object Accruals extends CashOrAccruals {
  override val cashOrAccruals = "accruals"
}

object CashOrAccruals {
  val feCash = "Cash"
  val feAccruals = "Accruals"

  private val reader: Reads[CashOrAccruals] = __.read[String].map {
    case `feCash` | Cash.cashOrAccruals => Cash
    case `feAccruals` | Accruals.cashOrAccruals => Accruals
  }

  private val writer: Writes[CashOrAccruals] = Writes[CashOrAccruals](cashOrAccruals =>
    JsString(cashOrAccruals.cashOrAccruals)
  )

  implicit val format: Format[CashOrAccruals] = Format(reader, writer)

  implicit def convert(str: String): CashOrAccruals = str match {
    case `feCash` | Cash.cashOrAccruals => Cash
    case `feAccruals` | Accruals.cashOrAccruals => Accruals
  }
}

case class BusinessDetailsModel
(
  accountingPeriodStartDate: String,
  accountingPeriodEndDate: String,
  tradingName: String,
  cashOrAccruals: CashOrAccruals
)

object BusinessDetailsModel {
  implicit val format: Format[BusinessDetailsModel] = Json.format[BusinessDetailsModel]
}

