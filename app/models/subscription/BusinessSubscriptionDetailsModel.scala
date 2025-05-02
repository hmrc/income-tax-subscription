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

package models.subscription

import models.DateModel
import play.api.libs.json._

case class AccountingPeriodModel(startDate: DateModel, endDate: DateModel) {
  lazy val taxEndYear: Int = AccountingPeriodUtil.getTaxEndYear(this)
  lazy val adjustedTaxYear: AccountingPeriodModel =
    if (taxEndYear <= 2018) {
      val nextStartDate = this.endDate.toLocalDate.plusDays(1)
      val nextEndDate = nextStartDate.plusYears(1).minusDays(1)
      AccountingPeriodModel(DateModel.dateConvert(nextStartDate), DateModel.dateConvert(nextEndDate))
    }
    else this

  val toItsaStatusShortTaxYear: String = s"${startDate.year}-${endDate.year.takeRight(2)}"
  val toShortTaxYear: String = s"${startDate.year.takeRight(2)}-${endDate.year.takeRight(2)}"
}

object AccountingPeriodModel {
  implicit val format: OFormat[AccountingPeriodModel] = Json.format[AccountingPeriodModel]
}

case class BusinessStartDate(startDate: DateModel)

object BusinessStartDate {
  implicit val format: OFormat[BusinessStartDate] = Json.format[BusinessStartDate]
}

case class BusinessNameModel(businessName: String)

object BusinessNameModel {
  implicit val format: OFormat[BusinessNameModel] = Json.format[BusinessNameModel]
}

case class BusinessTradeNameModel(businessTradeName: String)

object BusinessTradeNameModel {
  implicit val format: OFormat[BusinessTradeNameModel] = Json.format[BusinessTradeNameModel]
}

case class Address(lines: Seq[String], postcode: Option[String]) {
  override def toString: String = s"${lines.mkString(", ")}, $postcode"
}

case class BusinessAddressModel(address: Address)

object Address {
  implicit val format: OFormat[Address] = Json.format[Address]
}

object BusinessAddressModel {
  implicit val format: OFormat[BusinessAddressModel] = Json.format[BusinessAddressModel]
}

case class  SelfEmploymentData(id: String,
                               startDateBeforeLimit: Boolean,
                               businessStartDate: Option[BusinessStartDate] = None,
                               businessName: Option[BusinessNameModel] = None,
                               businessTradeName: Option[BusinessTradeNameModel] = None,
                               businessAddress: Option[BusinessAddressModel] = None)

object SelfEmploymentData {
  implicit val format: Format[SelfEmploymentData] = Json.format[SelfEmploymentData]
}


