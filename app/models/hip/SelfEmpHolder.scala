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

import models.hip.Converter.Utils
import models.subscription.Address
import models.{DateModel, PrePopSelfEmployment}
import play.api.libs.json.{Json, OFormat}

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
  private[models] def toPrePopSelfEmployment(): PrePopSelfEmployment = PrePopSelfEmployment(
    name = businessName,
    trade = businessDescription,
    address = Some(Address(
      lines = Seq(businessAddressFirstLine).flatten,
      postcode = businessAddressPostcode
    )),
    startDate = dateBusinessStarted.toDate(),
    accountingMethod = None
  )
}

object SelfEmp {
  implicit val format: OFormat[SelfEmp] = Json.format[SelfEmp]
}

object Converter {
  implicit class Utils(value: Option[String]) {
    def toDate(): Option[DateModel] = {
      value.map(date => DateModel(
        day = date.substring(8, 10),
        month = date.substring(5, 7),
        year = date.substring(0, 4)
      ))
    }
  }
}
