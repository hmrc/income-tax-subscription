/*
 * Copyright 2022 HM Revenue & Customs
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

import models.subscription.incomesource.SignUpRequest
import services.monitoring.AuditModel

case class RegistrationRequestAudit(signUpRequest: SignUpRequest, urlHeaderAuthorization: String) extends AuditModel {

  val nino: String = signUpRequest.nino
  val isAgent: String = signUpRequest.isAgent.toString
  val agentReferenceNumber: String = signUpRequest.arn.getOrElse("-")
  val sourceOfIncome: String = (signUpRequest.businessIncome, signUpRequest.propertyIncome) match {
    case (Some(_), Some(_)) => "Both"
    case (Some(_), None) => "Business"
    case (None, Some(_)) => "Property"
    case _ => "-"
  }
  val accountingPeriodStartDate: String = signUpRequest.businessIncome.map(_.accountingPeriod.startDate.toDesDateFormat).getOrElse("-")
  val accountingPeriodEndDate: String = signUpRequest.businessIncome.map(_.accountingPeriod.endDate.toDesDateFormat).getOrElse("-")
  val tradingName: String = signUpRequest.businessIncome.flatMap(_.tradingName).getOrElse("-")
  val cashOrAccruals: String = signUpRequest.businessIncome.map(_.accountingMethod.stringValue).getOrElse("-")
  val propertyCashOrAccruals: String = signUpRequest.propertyIncome.map(_.accountingMethod.stringValue).getOrElse("-")

  override val auditType: String = RegistrationRequestAudit.auditType
  override val transactionName: String = RegistrationRequestAudit.transactionName
  override val detail: Map[String, String] = Map(
    "nino" -> nino,
    "isAgent" -> isAgent,
    "agentReferenceNumber" -> agentReferenceNumber,
    "sourceOfIncome" -> sourceOfIncome,
    "acccountingPeriodStartDate" -> accountingPeriodStartDate,
    "acccountingPeriodEndDate" -> accountingPeriodEndDate,
    "tradingName" -> tradingName,
    "cashOrAccruals" -> cashOrAccruals,
    "propertyCashOrAccruals" -> propertyCashOrAccruals,
    "Authorization" -> urlHeaderAuthorization
  )

}

object RegistrationRequestAudit {

  val transactionName: String = "Customer-subscribed-to-send-quarterly-SA-income-tax-reports"
  val auditType: String = "mtdItsaSubscription"

}
