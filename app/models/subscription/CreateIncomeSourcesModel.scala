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
import models.subscription.business.AccountingMethod
import play.api.libs.json._
import uk.gov.hmrc.http.InternalServerException
import utils.JsonUtils.JsObjectUtil

case class CreateIncomeSourcesModel(nino: String,
                                    soleTraderBusinesses: Option[SoleTraderBusinesses] = None,
                                    ukProperty: Option[UkProperty] = None,
                                    overseasProperty: Option[OverseasProperty] = None)

object CreateIncomeSourcesModel {

  private def throwError(field: String) = {
    throw new InternalServerException(s"[CreateIncomeSourcesModel] - Unable to create model, $field is missing")
  }

  def addressDetailsJson(businessAddressModel: BusinessAddressModel): JsObject = {
    Json.obj(
      "addressLine1" -> businessAddressModel.address.lines.headOption.getOrElse(throwError("addressLine1")),
      "countryCode" -> "GB"
    ) ++
      businessAddressModel.address.postcode.map(value => Json.obj("postalCode" -> value)) ++
      businessAddressModel.address.lines.lift(1).map(value => Json.obj("addressLine2" -> value)) ++
      businessAddressModel.address.lines.lift(2).map(value => Json.obj("addressLine3" -> value)) ++
      businessAddressModel.address.lines.lift(3).map(value => Json.obj("addressLine4" -> value))
  }

  private def businessDetailsJson(soleTraderBusinesses: SoleTraderBusinesses): JsObject = {
    Json.obj(
      "businessDetails" -> soleTraderBusinesses.businesses.map { business =>
        Json.obj(
          "accountingPeriodStartDate" -> soleTraderBusinesses.accountingPeriod.startDate.toDesDateFormat,
          "accountingPeriodEndDate" -> soleTraderBusinesses.accountingPeriod.endDate.toDesDateFormat,
          "tradingName" -> business.businessName.map(_.businessName).getOrElse(throwError("businessName")),
          "addressDetails" -> business.businessAddress.map(addressDetailsJson).getOrElse(throwError("addressDetails")),
          "typeOfBusiness" -> business.businessTradeName.map(_.businessTradeName).getOrElse(throwError("tradingName")),
          "tradingStartDate" -> business.businessStartDate.map(_.startDate.toDesDateFormat).getOrElse(throwError("tradingStartDate")),
          "cashOrAccrualsFlag" -> soleTraderBusinesses.accountingMethod.stringValue.toUpperCase
        )
      }
    )
  }

  private def ukPropertyJson(ukProperty: UkProperty): JsObject = {
    Json.obj(
      "ukPropertyDetails" -> Json.obj(
        "tradingStartDate" -> ukProperty.tradingStartDate.toDesDateFormat,
        "cashOrAccrualsFlag" -> ukProperty.accountingMethod.stringValue.toUpperCase,
        "startDate" -> ukProperty.accountingPeriod.startDate.toDesDateFormat
      )
    )
  }

  private def overseasProperty(overseasProperty: OverseasProperty): JsObject = {
    Json.obj(
      "foreignPropertyDetails" -> Json.obj(
        "tradingStartDate" -> overseasProperty.tradingStartDate.toDesDateFormat,
        "cashOrAccrualsFlag" -> overseasProperty.accountingMethod.stringValue.toUpperCase,
        "startDate" -> overseasProperty.accountingPeriod.startDate.toDesDateFormat
      )
    )
  }

  implicit val reads: Reads[CreateIncomeSourcesModel] = Json.reads[CreateIncomeSourcesModel]
  implicit val writes: OWrites[CreateIncomeSourcesModel] = OWrites { createIncomeSourcesModel =>
    Json.obj() ++
      createIncomeSourcesModel.soleTraderBusinesses.map(businessDetailsJson) ++
      createIncomeSourcesModel.ukProperty.map(ukPropertyJson) ++
      createIncomeSourcesModel.overseasProperty.map(overseasProperty)
  }

}

case class SoleTraderBusinesses(accountingPeriod: AccountingPeriodModel,
                                accountingMethod: AccountingMethod,
                                businesses: Seq[SelfEmploymentData])

object SoleTraderBusinesses {
  implicit val reads: Reads[SoleTraderBusinesses] = Json.reads[SoleTraderBusinesses]
}

case class UkProperty(accountingPeriod: AccountingPeriodModel,
                      tradingStartDate: DateModel,
                      accountingMethod: AccountingMethod)

object UkProperty {
  implicit val reads: Reads[UkProperty] = Json.reads[UkProperty]
}

case class OverseasProperty(accountingPeriod: AccountingPeriodModel,
                            tradingStartDate: DateModel,
                            accountingMethod: AccountingMethod)

object OverseasProperty {
  implicit val reads: Reads[OverseasProperty] = Json.reads[OverseasProperty]
}

