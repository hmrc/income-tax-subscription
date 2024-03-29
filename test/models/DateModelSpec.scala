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

package models

import common.CommonSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DateModelSpec extends CommonSpec with Matchers {

  "the DateModel" should {

    val date = DateModel("01", "02", "2017")

    "convert correctly to java.time.LocalDate" in {
      date.toLocalDate shouldBe LocalDate.parse("01/02/2017", DateTimeFormatter.ofPattern("dd/MM/yyyy"))
      date.toLocalDate.isEqual(date) shouldBe true
    }

    "correctly format a date output to a view into d MMMMM uuuu" in {
      date.toOutputDateFormat shouldBe "1 February 2017"
    }

    "correctly format a date to DES format uuuu-MM-dd" in {
      date.toDesDateFormat shouldBe "2017-02-01"
    }

    "correctly determine the difference in months between two dates" in {
      val date1 = DateModel("01","03","2017")
      date.diffInMonth(date1) shouldBe 1
    }
  }
}
