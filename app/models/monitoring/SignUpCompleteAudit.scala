/*
 * Copyright 2021 HM Revenue & Customs
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


import models.subscription.{BusinessSubscriptionDetailsModel, SelfEmploymentData}
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}
import services.monitoring.ExtendedAuditModel

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

  override val auditType: String = RegistrationRequestAudit.auditType
  override val transactionName: String = RegistrationRequestAudit.transactionName
  override val detail: JsValue = Json.obj(
    "nino" -> nino,
    "userType" -> userType,
    "taxYear" -> taxYear,
    "income" -> income,
    "Authorization" -> urlHeaderAuthorization
  ) ++ optionToJson("agentReferenceNumber", agentReferenceNumber)
}

object SignUpCompleteAuditAudit {

  val transactionName: String = "income-tax-subscription"
  val auditType: String = "mtdItsaSubscription"

}
