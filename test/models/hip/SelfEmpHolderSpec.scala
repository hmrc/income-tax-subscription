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

package models.hip

import models.subscription.Address
import models.{DateModel, PrePopData, PrePopSelfEmployment}
import org.scalatestplus.play.PlaySpec

class SelfEmpHolderSpec extends PlaySpec {
  "toPrePopSelfEmployment" should {
    val empty = SelfEmpHolder(
      selfEmp = SelfEmp(
        businessName = None,
        businessDescription = None,
        businessAddressFirstLine = None,
        businessAddressPostcode = None,
        dateBusinessStarted = None
      )
    )

    val data = SelfEmpHolder(
      selfEmp = SelfEmp(
        businessName = Some("ABC Plumbers"),
        businessDescription = Some("Plumber"),
        businessAddressFirstLine = Some("1 Hazel Court"),
        businessAddressPostcode = Some("AB12 3CD"),
        dateBusinessStarted = Some("2011-08-14")
      )
    )

    val expected = Map(data -> PrePopSelfEmployment(
      name = Some("ABC Plumbers"),
      trade = Some("Plumber"),
      address = Some(Address(
        Seq("1 Hazel Court"),
        Some("AB12 3CD")
      )),
      startDate = Some(DateModel("14", "08", "2011")),
      accountingMethod = None
    ), empty -> PrePopSelfEmployment(
        name = None,
        trade = None,
        address = Some(Address(
          Seq.empty,
          None
        )),
        startDate = None,
        accountingMethod = None
      )
    )

    "convert HIP propop data to non-HIP prepop data" in {
      Seq(empty, data).foreach { data =>
        val actual = Some(data.toPrePopSelfEmployment())
        actual mustBe expected.get(data)
      }
    }
  }
}
