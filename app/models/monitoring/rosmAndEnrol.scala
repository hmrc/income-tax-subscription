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

package models.monitoring

import models.frontend.FERequest
import services.monitoring.AuditModel


object rosmAndEnrol {
  val controlListTransactionName = "Customer-subscribed-to-send-quarterly-SA-income-tax-reports"
  val controlListAuditType = "mtdItsaSubscription"

  case class rosmAndEnrolModel(fERequest: FERequest,
                               urlHeaderAuthorization: String
                              ) extends AuditModel {
    override val transactionName: String = controlListTransactionName
    override val detail: Map[String, String] = Map(
      "nino" -> fERequest.nino,
      "isAgent" -> fERequest.isAgent.toString,
      "arn" -> fERequest.arn.fold("-")(identity),
      "sourceOfIncome" -> fERequest.incomeSource.toString,
      "acccountingPeriodStartDate" -> fERequest.accountingPeriodStart.fold("-")(x => x.toDesDateFormat),
      "acccountingPeriodEndDate" -> fERequest.accountingPeriodEnd.fold("-")(x => x.toDesDateFormat),
      "tradingName" -> fERequest.tradingName.fold("-")(identity),
      "cashOrAccruals" -> fERequest.cashOrAccruals.fold("-")(x => x.toLowerCase),
      "Authorization" -> urlHeaderAuthorization
    )

    override val auditType: String = controlListAuditType
  }

}
