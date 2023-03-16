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

import play.api.libs.json.JsObject
import services.monitoring.AuditModel
import utils.Logging

case class PropertySubscribeFailureAudit(nino: String,
                                         arn: Option[String],
                                         propertySubscriptionPayload: JsObject,
                                         responseBody: String) extends AuditModel {

  override val auditType: String = s"${PropertySubscribeFailureAudit.transactionName}-${Logging.eventTypeUnexpectedError}"
  override val transactionName: Option[String] = Some(PropertySubscribeFailureAudit.transactionName)
  override val detail: Map[String, String] = Map(
    "nino" -> nino,
    "arn" -> arn.getOrElse("-"),
    "subscribe" -> propertySubscriptionPayload.toString,
    "response" -> responseBody
  )

}

object PropertySubscribeFailureAudit {

  val transactionName: String = "property-subscribe-api-35"

}
