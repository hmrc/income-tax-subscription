/*
 * Copyright 2020 HM Revenue & Customs
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
import models.subscription.business.AccountingMethod
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

case class Address(lines: Seq[String], postcode: String) {
  override def toString: String = s"${lines.mkString(", ")}, $postcode"
}

case class BusinessAddressModel(auditRef: String,
                                address: Address)

object Address {
  implicit val format: OFormat[Address] = Json.format[Address]
}

object BusinessAddressModel {
  implicit val format: OFormat[BusinessAddressModel] = Json.format[BusinessAddressModel]
}

case class  SelfEmploymentData(id: String,
                               businessStartDate: Option[BusinessStartDate] = None,
                               businessName: Option[BusinessNameModel] = None,
                               businessTradeName: Option[BusinessTradeNameModel] = None,
                               businessAddress: Option[BusinessAddressModel] = None)

object SelfEmploymentData {
  implicit val format: Format[SelfEmploymentData] = Json.format[SelfEmploymentData]
}

case class FeIncomeSourceModel(selfEmployment: Boolean,
                               ukProperty: Boolean,
                               foreignProperty: Boolean)

object FeIncomeSourceModel {
  implicit val format: Format[FeIncomeSourceModel] = Json.format[FeIncomeSourceModel]
}

case class PropertyCommencementDateModel(startDate: DateModel)

object PropertyCommencementDateModel {
  implicit val format: OFormat[PropertyCommencementDateModel] = Json.format[PropertyCommencementDateModel]
}

case class AccountingMethodPropertyModel(propertyAccountingMethod: AccountingMethod)

object AccountingMethodPropertyModel {
  implicit val format: OFormat[AccountingMethodPropertyModel] = Json.format[AccountingMethodPropertyModel]
}

case class OverseasPropertyCommencementDateModel(startDate: DateModel)

object OverseasPropertyCommencementDateModel {
  implicit val format: OFormat[OverseasPropertyCommencementDateModel] = Json.format[OverseasPropertyCommencementDateModel]
}

case class OverseasAccountingMethodPropertyModel(overseasPropertyAccountingMethod: AccountingMethod)

object OverseasAccountingMethodPropertyModel {
  implicit val format: OFormat[OverseasAccountingMethodPropertyModel] = Json.format[OverseasAccountingMethodPropertyModel]
}

case class BusinessSubscriptionDetailsModel(accountingPeriod: AccountingPeriodModel,
                                            selfEmploymentsData: Seq[SelfEmploymentData],
                                            accountingMethod: AccountingMethod,
                                            incomeSource: FeIncomeSourceModel,
                                            propertyCommencementDate: Option[PropertyCommencementDateModel] = None,
                                            propertyAccountingMethod: Option[AccountingMethodPropertyModel] = None,
                                            overseasPropertyCommencementDate: Option[OverseasPropertyCommencementDateModel] = None,
                                            overseasAccountingMethodProperty: Option[OverseasAccountingMethodPropertyModel] = None
                                           )

object BusinessSubscriptionDetailsModel {

  implicit val businessSubscriptionReads: Reads[BusinessSubscriptionDetailsModel] = Json.reads[BusinessSubscriptionDetailsModel]

  def withoutValue(value: JsValue): Boolean= value match {
    case JsNull => true
    case JsString("") => true
    case _ => false
  }

  implicit val businessSubscriptionWrites: Writes[BusinessSubscriptionDetailsModel] = new Writes[BusinessSubscriptionDetailsModel] {
    def writes(details: BusinessSubscriptionDetailsModel): JsObject = JsObject(Json.obj(
      "businessDetails" -> details.selfEmploymentsData.map(
        data => Map("accountingPeriodStartDate" -> JsString(details.accountingPeriod.startDate.toDesDateFormat),
          "accountingPeriodEndDate" -> JsString(details.accountingPeriod.endDate.toDesDateFormat),
          "tradingName" -> JsString(data.businessTradeName.map(_.businessTradeName).getOrElse(throw new Exception("Missing tradingName parameter"))),
          "addressDetails" -> JsObject(Json.obj(
            "addressLine1" -> data.businessAddress.map(_.address.lines.head).getOrElse(throw new Exception("Missing addressLine1 parameter")),
            "addressLine2" -> data.businessAddress.map(model => if(model.address.lines.length > 1) model.address.lines(1) else ""),
            "addressLine3" -> data.businessAddress.map(model => if(model.address.lines.length > 2) model.address.lines(2) else ""),
            "addressLine4" -> data.businessAddress.map(model => if(model.address.lines.length > 3) model.address.lines(3) else ""),
            "postalCode" -> data.businessAddress.map(_.address.postcode).getOrElse(throw new Exception("Missing postalCode parameter")),
            "countryCode" -> "GB"
          ).fields.filterNot(json => withoutValue(json._2))),
          "typeOfBusiness" -> JsString("self-employment"),
          "tradingStartDate" -> JsString(data.businessStartDate.getOrElse(
            throw new Exception("Missing businessStartDate Parameter")).startDate.toDesDateFormat),
          "cashOrAccrualsFlag" -> JsString(details.accountingMethod.stringValue.toUpperCase)
        )),
      "ukPropertyDetails" -> Json.toJson(
        if(details.incomeSource.ukProperty)
          Json.obj(
            "tradingStartDate" -> details.propertyCommencementDate.map(_.startDate.toDesDateFormat),
            "cashOrAccrualsFlag" -> details.propertyAccountingMethod.map(_.propertyAccountingMethod.stringValue.toUpperCase),
            "startDate" -> details.accountingPeriod.startDate.toDesDateFormat
          )
        else JsNull
      ),
      "foreignPropertyDetails" -> Json.toJson(
        if(details.incomeSource.foreignProperty)
          Json.obj(
          "tradingStartDate" -> details.overseasPropertyCommencementDate.map(_.startDate.toDesDateFormat),
          "cashOrAccrualsFlag" -> details.overseasAccountingMethodProperty.map(_.overseasPropertyAccountingMethod.stringValue.toUpperCase),
          "startDate" -> details.accountingPeriod.startDate.toDesDateFormat
          )
        else JsNull
      )
    ).fields.filterNot(json => withoutValue(json._2)))
  }
}

