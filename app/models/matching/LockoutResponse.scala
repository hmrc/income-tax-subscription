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

package models.matching

import java.time._

import play.api.libs.json.{JsObject, OFormat, OWrites, _}

case class LockoutResponse(arn: String, expiryTimestamp: OffsetDateTime)

object LockoutResponse {
  val arn = "_id"
  val expiry = "expiryTimestamp"

  implicit val temporalReads: Reads[OffsetDateTime] = new Reads[OffsetDateTime] {
    override def reads(json: JsValue): JsResult[OffsetDateTime] = {
      (json \ "$date").validate[Long] map (millis =>
        OffsetDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()))
    }
  }

  implicit val temporalWrites: Writes[OffsetDateTime] = new Writes[OffsetDateTime] {
    override def writes(o: OffsetDateTime): JsValue = Json.obj("$date" -> Instant.from(o).toEpochMilli)
  }

  val reader: Reads[LockoutResponse] = new Reads[LockoutResponse] {
    override def reads(json: JsValue): JsResult[LockoutResponse] = for {
      arn <- (json \ arn).validate[String]
      exp <- (json \ expiry).validate[OffsetDateTime]
    } yield LockoutResponse(arn, exp)
  }

  val writer: OWrites[LockoutResponse] = new OWrites[LockoutResponse] {
    override def writes(o: LockoutResponse): JsObject =
      Json.obj(arn -> o.arn, expiry -> Json.toJson(o.expiryTimestamp))
  }

  implicit val format: OFormat[LockoutResponse] = OFormat[LockoutResponse](reader, writer)

  val feWritter: OWrites[LockoutResponse] = new OWrites[LockoutResponse] {
    override def writes(o: LockoutResponse): JsObject =
      Json.obj("arn" -> o.arn, expiry -> o.expiryTimestamp.toString)
  }

}
