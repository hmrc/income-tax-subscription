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

package models.status

import play.api.libs.json._

sealed trait ITSAStatus {
  def value: String
}

object ITSAStatus {

  case object MTDMandated extends ITSAStatus {
    override val value: String = "MTD Mandated"
  }

  case object MTDVoluntary extends ITSAStatus {
    override val value: String = "MTD Voluntary"
  }

  implicit val reads: Reads[ITSAStatus] = __.read[String] flatMapResult {
    case MTDMandated.value | "01" => JsSuccess(MTDMandated)
    case MTDVoluntary.value | "02" => JsSuccess(MTDVoluntary)
    case _ => JsError("Unsupported itsa status")
  }

  implicit val writes: Writes[ITSAStatus] = Writes[ITSAStatus] { status =>
    JsString(status.value)
  }
}
