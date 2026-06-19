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

import play.api.libs.json.{JsValue, Json}
import services.monitoring.ExtendedAuditModel
import utils.JsonUtils.JsObjectUtil

case class CreateIncomeSourcesAudit(agentReferenceNumber: Option[String],
                                    nino: String,
                                    mtdItsaReferenceNumber: String,
                                    isSuccess: Boolean,
                                    errorReason: Option[String]) extends ExtendedAuditModel {

  val userType: String = if (agentReferenceNumber.isDefined) "agent" else "individual"

  override val auditType: String = "ITSACreateIncomeSourcesOutcome"
  override val transactionName: Option[String] = None

  override val detail: JsValue = {
    val base = Json.obj(
      "userType"               -> userType,
      "nino"                   -> nino,
      "mtdItsaReferenceNumber" -> mtdItsaReferenceNumber,
      "incomeSourcesCreate"    -> (if (isSuccess) "Success" else "Failure")
    ) ++ agentReferenceNumber.map(arn => Json.obj("agentReferenceNumber" -> arn))

    errorReason match {
      case Some(reason) => base ++ Json.obj("errorReason" -> CreateIncomeSourcesAudit.sanitiseErrorReason(reason))
      case None         => base
    }
  }
}

object CreateIncomeSourcesAudit {
  private[monitoring] def sanitiseErrorReason(reason: String): String = {
    val statusPrefix = "Status: "
    val statusIndex = reason.indexOf(statusPrefix)
    if (statusIndex >= 0) reason.substring(statusIndex).trim else reason
  }
}
