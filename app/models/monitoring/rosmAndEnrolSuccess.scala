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

import models.frontend.{FERequest, FESuccessResponse}
import services.monitoring.AuditModel


object rosmAndEnrolSuccess {
  val controlListTransactionName = "Customer-subscribed-mtd-itsa-reference-number"
  val controlListAuditType = "mtdItsaSubscriptionReferenceNumber"

  case class rosmAndEnrolSuccessModel(fERequest: FERequest,
                                      fESuccessResponse: FESuccessResponse,
                                      pathKey: String
                                     ) extends AuditModel {
    override val transactionName: String = controlListTransactionName
    override val detail: Map[String, String] = Map(
      "nino" -> fERequest.nino,
      "arn" -> fERequest.arn.fold("-")(identity),
      "mtdItsaReferenceNumber" -> fESuccessResponse.mtditId.get,
      "pathKey" -> pathKey
    )

    override val auditType: String = controlListAuditType
  }

}
