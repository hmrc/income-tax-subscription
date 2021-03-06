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

import play.api.http.Status._
import play.api.libs.json.JsObject
import services.monitoring.AuditModel
import utils.Logging._

case class RegistrationFailureAudit(nino: String,
                                    status: Int,
                                    registration: JsObject,
                                    responseBody: String) extends AuditModel {

  private val suffix: String = status match {
    case BAD_REQUEST => eventTypeBadRequest
    case NOT_FOUND => eventTypeNotFound
    case CONFLICT => eventTypeConflict
    case INTERNAL_SERVER_ERROR => eventTypeInternalServerError
    case SERVICE_UNAVAILABLE => eventTypeServerUnavailable
    case _ => eventTypeUnexpectedError
  }

  override val auditType: String = s"${RegistrationFailureAudit.transactionName}-$suffix"
  override val transactionName: String = RegistrationFailureAudit.transactionName
  override val detail: Map[String, String] = Map(
    "nino" -> nino,
    "requestJson" -> registration.toString,
    "response" -> responseBody
  )

}

object RegistrationFailureAudit {

  val transactionName: String = "register-api-4"

}
