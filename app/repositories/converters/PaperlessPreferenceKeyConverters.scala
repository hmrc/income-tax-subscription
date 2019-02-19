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

package repositories.converters

import java.time.Instant

import models.digitalcontact.PaperlessPreferenceKey
import play.api.libs.json.{Json, _}

object PaperlessPreferenceKeyReads extends Reads[PaperlessPreferenceKey] {
  override def reads(json: JsValue): JsResult[PaperlessPreferenceKey] = for {
    token <- (json \ "_id").validate[String]
    nino <- (json \ "nino").validate[String]
  } yield PaperlessPreferenceKey(token, nino)
}

object PaperlessPreferenceKeyWrites extends OWrites[PaperlessPreferenceKey] {
  override def writes(model: PaperlessPreferenceKey): JsObject = Json.obj(
    tokenKey -> model.token,
    ninoKey -> model.nino,
    timestampKey -> Json.obj("$date" -> Instant.now.toEpochMilli)
  )

  val tokenKey = "_id"
  val ninoKey = "nino"
  val timestampKey = "creationTimestamp"
}
