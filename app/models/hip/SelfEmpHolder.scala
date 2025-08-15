/*
 * Copyright 2024 HM Revenue & Customs
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

package models.hip

import models.PrePopSelfEmployment
import play.api.libs.json.{Json, OFormat, OWrites, Reads, __}

case class SelfEmpHolder(
  selfEmp: SelfEmp
) {
  def toPrePopSelfEmployment(): PrePopSelfEmployment =
    selfEmp.toPrePopSelfEmployment()
}

object SelfEmpHolder {
  implicit val format: OFormat[SelfEmpHolder] = Json.format[SelfEmpHolder]
}

case class SelfEmp(
  businessName: Option[String],
  businessDescription: Option[String],
  businessAddressFirstLine: Option[String],
  businessAddressPostcode: Option[String],
  dateBusinessStarted: Option[String]
) {
  private[models] def toPrePopSelfEmployment(): PrePopSelfEmployment = PrePopSelfEmployment.fromApi(
    name = businessName,
    trade = businessDescription.getOrElse(""),
    addressFirstLine = businessAddressFirstLine,
    addressPostcode = businessAddressPostcode,
    startDate = dateBusinessStarted,
    accountingMethod = ""
  )
}

object SelfEmp {
  implicit val format: OFormat[SelfEmp] = Json.format[SelfEmp]
}

case class SelfEmpResponse(selfEmp: Seq[SelfEmp])

object SelfEmpResponse {
  implicit val reads: Reads[SelfEmpResponse] = (__ \ "selfEmp").read[Seq[SelfEmp]].map(SelfEmpResponse(_))

  implicit val writes: OWrites[SelfEmpResponse] = Json.writes[SelfEmpResponse]
}