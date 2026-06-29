/*
 * Copyright 2025 HM Revenue & Customs
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

package models

import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsSuccess, Json}

class SignUpRequestSpec extends PlaySpec {

  private val data1: SignUpRequest = SignUpRequest(
    nino = "test-nino",
    utr = "test-utr",
    taxYear = "2024-2025"
  )

  private val data2 = data1.copy(
    idempotencyKey = Some("1234")
  )

  private val json1 = Json.obj(
    "nino" -> "test-nino",
    "utr" -> "test-utr",
    "taxYear" -> "2024-2025"
  )

  private val json2 = json1 ++ Json.obj(
    "idempotencyKey" -> "1234"
  )

  private val data = Map(
    "without [idemPotencyKey]" -> (data1, json1),
    "with [idemPotencyKey" -> (data2, json2)
  )
  
  "SignUpRequest" must {
    
    data.foreach { case (k, v) =>
      s"successfully read from json ($k)" in {
        Json.fromJson[SignUpRequest](v._2) mustBe JsSuccess(v._1)
      }

      s"successfully write to json ($k)" in {
        Json.toJson(v._1) mustBe v._2
      }
    }
  }
}
