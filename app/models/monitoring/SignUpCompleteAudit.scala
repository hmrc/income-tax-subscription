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

package models.monitoring

import models.subscription.{BusinessSubscriptionDetailsModel, CreateIncomeSourcesModel, SelfEmploymentData}
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}
import services.monitoring.ExtendedAuditModel
import utils.JsonUtils.JsObjectUtil

case class CompletedSignUpAudit(agentReferenceNumber: Option[String],
                                createIncomeSourcesModel: CreateIncomeSourcesModel,
                                urlHeaderAuthorization: String) extends ExtendedAuditModel {

  implicit class JsArrayUtil(jsArray: JsArray) {
    def ++(optJson: Option[JsObject]): JsArray = {
      optJson match {
        case Some(json) => jsArray :+ json
        case None => jsArray
      }
    }
  }

  val nino: String = createIncomeSourcesModel.nino
  val userType: String = if (agentReferenceNumber.isDefined) {
    "agent"
  } else {
    "individual"
  }

  val taxYear: String = (createIncomeSourcesModel.soleTraderBusinesses.map(_.accountingPeriod) orElse
    createIncomeSourcesModel.ukProperty.map(_.accountingPeriod) orElse
    createIncomeSourcesModel.overseasProperty.map(_.accountingPeriod))
    .map(accountingPeriod => s"${accountingPeriod.taxEndYear - 1}-${accountingPeriod.taxEndYear}").getOrElse("-")

  val soleTraderIncomeSources: Option[JsObject] = createIncomeSourcesModel.soleTraderBusinesses.map { soleTraderBusinesses =>
    Json.obj(
      "incomeSource" -> "selfEmployment",
      "businesses" -> soleTraderBusinesses.businesses.map { business =>
        Json.obj() ++
          business.businessStartDate.map(businessStartDate => Json.obj("businessCommencementDate" -> businessStartDate.startDate.toDesDateFormat)) ++
          business.businessName.map(businessName => Json.obj("businessName" -> businessName.businessName)) ++
          business.businessTradeName.map(businessTrade => Json.obj("businessTrade" -> businessTrade.businessTradeName)) ++
          business.businessAddress.map { businessAddress =>
            Json.obj(
              "businessAddress" -> Json.obj(
                "lines" -> businessAddress.address.lines,
                "postcode" -> businessAddress.address.postcode
              )
            )
          }
      },
      "accountingType" -> soleTraderBusinesses.accountingMethod.stringValue.toLowerCase,
      "numberOfBusinesses" -> soleTraderBusinesses.businesses.length.toString
    )
  }

  val ukPropertyIncomeSource: Option[JsObject] = createIncomeSourcesModel.ukProperty.map { ukProperty =>
    Json.obj(
      "incomeSource" -> "ukProperty",
      "commencementDate" -> ukProperty.tradingStartDate.toDesDateFormat,
      "accountingType" -> ukProperty.accountingMethod.stringValue.toLowerCase
    )
  }

  val overseasPropertyIncomeSource: Option[JsObject] = createIncomeSourcesModel.overseasProperty.map { overseasProperty =>
    Json.obj(
      "incomeSource" -> "foreignProperty",
      "commencementDate" -> overseasProperty.tradingStartDate.toDesDateFormat,
      "accountingType" -> overseasProperty.accountingMethod.stringValue.toLowerCase
    )
  }

  val income: JsArray = Json.arr() ++ soleTraderIncomeSources ++ ukPropertyIncomeSource ++ overseasPropertyIncomeSource

  override val auditType: String = "mtdItsaSubscription"
  override val transactionName: Option[String] = Some("Customer-subscribed-to-send-quarterly-SA-income-tax-reports")

  override val detail: JsValue = Json.obj(
    "nino" -> nino,
    "userType" -> userType,
    "taxYear" -> taxYear,
    "income" -> income,
    "Authorization" -> urlHeaderAuthorization
  ) ++ agentReferenceNumber.map(value => Json.obj("agentReferenceNumber" -> value))

}

case class SignUpCompleteAudit(agentReferenceNumber: Option[String],
                               businessSubscriptionDetailsModel: BusinessSubscriptionDetailsModel,
                               urlHeaderAuthorization: String) extends ExtendedAuditModel {

  implicit class JsArrayUtil(jsArray: JsArray) {
    def addIfTrue(flag: Boolean, json: JsObject): JsArray = {
      if (flag) jsArray :+ json else jsArray
    }
  }

  def optionToJson(key: String, optValue: Option[String]): JsObject = optValue match {
    case Some(value) => Json.obj(key -> value)
    case None => Json.obj()
  }

  val nino: String = businessSubscriptionDetailsModel.nino
  val userType: String = if (agentReferenceNumber.isDefined) {
    "agent"
  } else {
    "individual"
  }

  val taxYear: String = s"${businessSubscriptionDetailsModel.accountingPeriod.taxEndYear - 1}-${businessSubscriptionDetailsModel.accountingPeriod.taxEndYear}"

  val ukPropertyIncomeSource: JsObject = Json.obj(
    "incomeSource" -> "ukProperty"
  ) ++ optionToJson("commencementDate", businessSubscriptionDetailsModel.propertyStartDate.map(_.startDate.toDesDateFormat)) ++
    optionToJson("accountingType", businessSubscriptionDetailsModel.propertyAccountingMethod.map(_.propertyAccountingMethod.stringValue.toLowerCase))

  val foreignPropertyIncomeSource: JsObject = Json.obj(
    "incomeSource" -> "foreignProperty"
  ) ++ optionToJson("commencementDate", businessSubscriptionDetailsModel.overseasPropertyStartDate.map(_.startDate.toDesDateFormat)) ++
    optionToJson("accountingType", businessSubscriptionDetailsModel.overseasAccountingMethodProperty.map(_.overseasPropertyAccountingMethod.
      stringValue.toLowerCase))

  val seBusinessIncomeSource: JsObject = Json.obj(
    "incomeSource" -> "selfEmployment",
    "businesses" -> businessSubscriptionDetailsModel.selfEmploymentsData.getOrElse(Nil).map(seBusinessJson)
  ) ++ optionToJson("accountingType", businessSubscriptionDetailsModel.accountingMethod.map(_.stringValue.toLowerCase)) ++
    optionToJson("numberOfBusinesses", businessSubscriptionDetailsModel.selfEmploymentsData.map(_.length.toString))

  val income: JsArray = Json.arr()
    .addIfTrue(businessSubscriptionDetailsModel.incomeSource.ukProperty, ukPropertyIncomeSource)
    .addIfTrue(businessSubscriptionDetailsModel.incomeSource.foreignProperty, foreignPropertyIncomeSource)
    .addIfTrue(businessSubscriptionDetailsModel.incomeSource.selfEmployment, seBusinessIncomeSource)

  def seBusinessJson(selfEmployment: SelfEmploymentData): JsObject = {
    optionToJson("businessCommencementDate", selfEmployment.businessStartDate.map(_.startDate.toDesDateFormat)) ++
      optionToJson("businessTrade", selfEmployment.businessTradeName.map(_.businessTradeName)) ++
      optionToJson("businessName", selfEmployment.businessName.map(_.businessName)) ++
      (selfEmployment.businessAddress match {
        case Some(businessAddress) => Json.obj(
          "businessAddress" -> Json.obj(
            "lines" -> businessAddress.address.lines,
            "postcode" -> businessAddress.address.postcode
          )
        )
        case None => Json.obj()
      })
  }

  override val auditType: String = "mtdItsaSubscription"
  override val transactionName: Option[String] = Some("Customer-subscribed-to-send-quarterly-SA-income-tax-reports")
  override val detail: JsValue = Json.obj(
    "nino" -> nino,
    "userType" -> userType,
    "taxYear" -> taxYear,
    "income" -> income,
    "Authorization" -> urlHeaderAuthorization
  ) ++ optionToJson("agentReferenceNumber", agentReferenceNumber)
}
