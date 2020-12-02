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

import services.monitoring.AuditModel

case class RegistrationSuccessAudit(agentReferenceNumber: Option[String], nino: String,
                                    mtditid: String, authToken: String,path: Option[String]) extends AuditModel {

  val userType = if (agentReferenceNumber.isDefined) "agent" else "individual"

  override val auditType: String = RegistrationSuccessAudit.auditType
  override val transactionName: String = RegistrationSuccessAudit.transactionName
  override val detail: Map[String, String] = Map(
    "userType" -> userType,
    "nino" -> nino,
    "mtdItsaReferenceNumber" -> mtditid,
    "Authorisation"-> authToken
  ) ++ agentReferenceNumber.map(arn => "agentReferenceNumber" -> arn) ++ path.map(path => "pathKey" -> path)

}

object RegistrationSuccessAudit {

  val auditType: String = "mtdItsaSubscriptionReferenceNumber"
  val transactionName: String = "Customer-subscribed-mtd-itsa-reference-number"

}
