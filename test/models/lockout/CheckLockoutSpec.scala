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

package models.lockout

import common.CommonSpec
import play.api.libs.json.Json

import java.time.{Instant, OffsetDateTime, ZoneId}
import CheckLockout._

class CheckLockoutSpec extends CommonSpec {

  "JSON parsing " should {
    // get date to millis precision as both Offset Date Time and Long
    val nowLong = Instant.from(OffsetDateTime.now()).toEpochMilli
    val now = OffsetDateTime.ofInstant(Instant.ofEpochMilli(nowLong), ZoneId.systemDefault())
    "correctly deserialise an arn and a time" in {
      val objectified = Json.parse(
        s"""
           |{
           |  "$arn": "banana",
           |  "$expiry": {
           |    "$dollarDate": $nowLong
           |  }
           |}
        """.stripMargin
      ).validate[CheckLockout].get
      val expected = CheckLockout("banana", now)
      objectified should be(expected)
    }
    "correctly serialise an arn and a time" in {
      val serialised = Json.toJson(CheckLockout("banana", now)).toString()
      val expected =
        s"""
           |{
           |  "$arn":"banana",
           |  "$expiry":{
           |    "$dollarDate":$nowLong
           |  }
           |}
      """.stripMargin.split("\n").map(s => s.trim).mkString("")
      serialised should be(expected)
    }
  }
}
