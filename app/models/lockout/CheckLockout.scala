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

package models.lockout

import java.time.{Instant, OffsetDateTime, ZoneId}

import play.api.libs.json._

case class CheckLockout(arn: String, expiryTimestamp: OffsetDateTime)

object CheckLockout {

  val arn = "_id"
  val expiry = "expiryTimestamp"

  val temporalReads: Reads[OffsetDateTime] = new Reads[OffsetDateTime] {
    override def reads(json: JsValue): JsResult[OffsetDateTime] = {
      (json \ "$date").validate[Long] map (millis =>
        OffsetDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()))
    }
  }

  val temporalWrites: Writes[OffsetDateTime] = new Writes[OffsetDateTime] {
    override def writes(o: OffsetDateTime): JsValue = Json.obj("$date" -> Instant.from(o).toEpochMilli)
  }

  val reader: Reads[CheckLockout] = new Reads[CheckLockout] {
    override def reads(json: JsValue): JsResult[CheckLockout] = {
      val arnv = (json \ arn).validate[String]
      val exp = (json \ expiry).validate[OffsetDateTime]
      JsSuccess(CheckLockout(arnv.get, exp.get))
    }
  }

  val writer: OWrites[CheckLockout] = new OWrites[CheckLockout] {
    override def writes(o: CheckLockout): JsObject =
      Json.obj(arn -> o.arn, expiry -> Json.obj("$date" -> Instant.from(o.expiryTimestamp).toEpochMilli))
  }

  implicit val oformats: OFormat[CheckLockout] = OFormat[CheckLockout](reader, writer)

}

