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

package models.matching

import play.api.libs.json._

import java.time._

case class LockoutResponse(arn: String, expiryTimestamp: Instant)

object LockoutResponse {
  val arn = "_id"
  val expiry = "expiryTimestamp"

  val reader: Reads[LockoutResponse] = (json: JsValue) => for {
    arn <- (json \ arn).validate[String]
    exp <- (json \ expiry \ "$date" \ "$numberLong").validate[String].map(_.toLong)
  } yield {
    LockoutResponse(arn, Instant.ofEpochMilli(exp))
  }

  val writer: OWrites[LockoutResponse] = (o: LockoutResponse) => Json.obj(
    arn -> o.arn,
    expiry -> Json.obj(
      "$date" -> o.expiryTimestamp.toEpochMilli
    )
  )

  implicit val format: OFormat[LockoutResponse] = OFormat[LockoutResponse](reader, writer)

  val feWritter: OWrites[LockoutResponse] = (o: LockoutResponse) => Json.obj("arn" -> o.arn, expiry -> o.expiryTimestamp.toString)

}
