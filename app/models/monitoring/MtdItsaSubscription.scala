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

package models.monitoring

import models.DateModel
import services.monitoring.AuditModel


object mtdItsaSubscription {
  val controlListTransactionName = "Customer-subscribed-to-send-quarterly-SA-income-tax-reports"
  val controlListAuditType = "mtdItsaSubscription"

  case class CtReferenceMatchAuditModel(nino: String,
                                        isAgent: String,
                                        arn: Option[String],
                                        sourceOfIncome: String,
                                        acccountingPeriodStartDate: Option[DateModel],
                                        acccountingPeriodEndDate: Option[DateModel],
                                        tradingName: Option[String],
                                        cashOrAccrualsAuthorization: Option[String],
                                        urlHeaderAuthorization: String
                                  ) extends AuditModel {
    override val transactionName: String = controlListTransactionName
    override val detail: Map[String, String] = Map(
      "nino" -> nino,
      "isAgent" -> isAgent.toString,
      "arn" -> arn.fold("-")(identity),
      "sourceOfIncome" -> sourceOfIncome.toString,
      "acccountingPeriodStartDate" -> acccountingPeriodStartDate.fold("-")(x => x.toDesDateFormat),
      "acccountingPeriodEndDate" -> acccountingPeriodEndDate.fold("-")(x => x.toDesDateFormat),
      "tradingName" -> tradingName.fold("-")(identity),
      "cashOrAccruals" -> cashOrAccrualsAuthorization.fold("-")(x => x.toLowerCase),
      "Authorization" -> urlHeaderAuthorization
    )

    override val auditType: String = controlListAuditType
  }

}
