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

import models.registration.RegistrationRequestModel
import play.api.libs.json.Json
import services.monitoring.AuditModel


object registerAudit {
  val controlListTransactionName = "register-api-4"

  case class registerAuditModel(nino: String,
                                suffix : String,
                                registration: RegistrationRequestModel,
                                responseBody: String
                              ) extends AuditModel {
    override val transactionName: String = controlListTransactionName
    override val detail: Map[String, String] = Map(
      "nino" -> nino,
      "requestJson" -> Json.toJson(registration).toString(),
      "response" -> responseBody
    )
    override val auditType: String = controlListTransactionName + "-" + suffix
  }

}
