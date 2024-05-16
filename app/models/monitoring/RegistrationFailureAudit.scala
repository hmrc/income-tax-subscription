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

import models.monitoring.RegistrationFailureAudit.suffix
import play.api.http.Status._
import services.monitoring.AuditModel
import utils.Logging._

case class RegistrationFailureAudit(nino: String,
                                    status: Int,
                                    reason: String) extends AuditModel {

  override val transactionName: Option[String] = Some(RegistrationFailureAudit.transactionName)
  override val auditType: String = s"${RegistrationFailureAudit.transactionName}-${suffix(status)}"
  override val detail: Map[String, String] = Map(
    "nino" -> nino,
    "response" -> reason
  )

}

object RegistrationFailureAudit {

  def suffix(status: Int): String = status match {
    case BAD_REQUEST => eventTypeBadRequest
    case NOT_FOUND => eventTypeNotFound
    case CONFLICT => eventTypeConflict
    case INTERNAL_SERVER_ERROR => eventTypeInternalServerError
    case SERVICE_UNAVAILABLE => eventTypeServerUnavailable
    case _ => eventTypeUnexpectedError
  }

  val transactionName: String = "register-api-4"

}
