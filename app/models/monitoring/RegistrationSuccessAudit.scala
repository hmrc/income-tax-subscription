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

import models.SignUpResponse.SignUpSuccess
import parsers.SignUpParser.PostSignUpResponse
import services.monitoring.AuditModel

case class RegistrationSuccessAudit(agentReferenceNumber: Option[String],
                                    nino: String,
                                    utr: String,
                                    postSignUpResponse: PostSignUpResponse,
                                    authToken: String,
                                    path: Option[String],
                                    submissionAuditUpdateEnabled: Boolean) extends AuditModel {

  val userType = if (agentReferenceNumber.isDefined) "agent" else "individual"

  override val auditType: String = RegistrationSuccessAudit.auditType
  override val transactionName: Option[String] = Some(RegistrationSuccessAudit.transactionName)

  val result: String = postSignUpResponse match {
    case Left(_) => "Failure"
    case Right(_) => "Success"
  }

  val mtditid: Option[String] = postSignUpResponse match {
    case Left(_) => None
    case Right(SignUpSuccess(mtdbsa)) => Some(mtdbsa)
  }

  val errorReason: Option[String] = postSignUpResponse match {
    case Left(error) => Some(RegistrationSuccessAudit.sanitiseErrorReason(error.reason))
    case Right(_) => None
  }

  override val detail: Map[String, String] = if (submissionAuditUpdateEnabled) {
    Map(
      "userType" -> Some(userType),
      "nino" -> Some(nino),
      "utr" -> Some(utr),
      "result" -> Some(result),
      "agentReferenceNumber" -> agentReferenceNumber,
      "mtdItsaReferenceNumber" -> mtditid,
      "errorReason" -> errorReason
    ) collect {
      case (key, Some(value)) => key -> value
    }
  } else {
    Map(
      "userType" -> userType,
      "nino" -> nino,
      "Authorisation" -> authToken
    ) ++ agentReferenceNumber.map("agentReferenceNumber" -> _) ++ path.map("pathKey" -> _) ++ mtditid.map("mtdItsaReferenceNumber" -> _)
  }

}

object RegistrationSuccessAudit {

  val auditType: String = "mtdItsaSubscriptionReferenceNumber"
  val transactionName: String = "Customer-subscribed-mtd-itsa-reference-number"

  private[monitoring] def sanitiseErrorReason(reason: String): String = {
    val statusPrefix = "Status: "
    val statusIndex = reason.indexOf(statusPrefix)

    if (statusIndex >= 0) reason.substring(statusIndex).trim else reason
  }

}
