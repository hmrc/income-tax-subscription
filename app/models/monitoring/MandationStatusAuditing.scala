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

import services.monitoring.AuditModel

case class MandationStatusAuditModel(agentReferenceNumber: Option[String],
                                     utr: String,
                                     nino: String,
                                     currentYear: String,
                                     currentYearStatus: String,
                                     nextYear: String,
                                     nextYearStatus: String
                                    ) extends AuditModel {

  val userType = if (agentReferenceNumber.isDefined) "agent" else "individual"

  override val auditType: String = MandationStatusAuditing.auditType
  override val transactionName: Option[String] = None
  override val detail: Map[String, String] = Map(
    "userType" -> userType,
    "saUtr" -> utr,
    "nino" -> nino,
    "CurrentYear" -> currentYear,
    "ITSAStatusCurrentYear" -> currentYearStatus,
    "NextYear" -> nextYear,
    "ITSAStatusNextYear" -> nextYearStatus
  ) ++ agentReferenceNumber.map(arn => "agentReferenceNumber" -> arn)
}

object MandationStatusAuditing {

  val auditType: String = "ITSAStatus"

}
