/*
 * Copyright 2023 HM Revenue & Customs
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
  val feCash = "Cash"
  val feAccruals = "Accruals"

  private val reader: Reads[AccountingMethod] = __.read[String].map (convert)

  private val writer: Writes[AccountingMethod] = Writes[AccountingMethod](cashOrAccruals =>
    JsString(cashOrAccruals.stringValue)
  )

  implicit val format: Format[AccountingMethod] = Format(reader, writer)

  implicit def convert(str: String): AccountingMethod = str match {
    case `feCash` | Cash.stringValue => Cash
    case `feAccruals` | Accruals.stringValue => Accruals
    case other => throw new Exception(s"Unknown AccountingMethod string: $other")
  }
}
