/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.libs.json.{JsString, Reads, Writes, __}

sealed trait MtdMandationStatus {
  def value: String
}

object MtdMandationStatus {

  private val mandated: String = "MTD Mandated"
  private val voluntary: String = "MTD Voluntary"

  case object Mandated extends MtdMandationStatus {
    override val value: String = mandated
  }

  case object Voluntary extends MtdMandationStatus {
    override def value: String = voluntary
  }

  implicit val reads: Reads[MtdMandationStatus] = __.read[String].map {
    case `mandated` => Mandated
    case `voluntary` => Voluntary
  }

  implicit val writes: Writes[MtdMandationStatus] = Writes[MtdMandationStatus] { status =>
    JsString(status.value)
  }

}

