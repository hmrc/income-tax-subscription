/*
 * Copyright 2024 HM Revenue & Customs
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

import models.subscription.Address
import models.subscription.business.{Accruals, Cash}
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsObject, Json}

class PrePopAuditModelSpec extends PlaySpec {

  val fullPrePopData: PrePopData = PrePopData(
    selfEmployment = Some(Seq(
      PrePopSelfEmployment(
        name = Some("ABC"),
        trade = Some("Plumbing"),
        address = Some(Address(
          lines = Seq(
            "1 long road"
          ),
          postcode = Some("ZZ1 1ZZ")
        )),
        startDate = Some(DateModel("12", "12", "1980")),
        accountingMethod = Cash
      )
    )),
    ukPropertyAccountingMethod = Some(Cash),
    foreignPropertyAccountingMethod = Some(Accruals)
  )
  val minimalPrePopData: PrePopData = PrePopData(
    selfEmployment = None,
    ukPropertyAccountingMethod = None,
    foreignPropertyAccountingMethod = None
  )
  val agentReferenceNumber: String = "test-arn"
  val nino: String = "test-nino"

  val fullAuditModel: PrePopAuditModel = PrePopAuditModel(fullPrePopData, nino, Some(agentReferenceNumber))
  val minimalAuditModel: PrePopAuditModel = PrePopAuditModel(minimalPrePopData, nino, None)

  "PrePopAuditModel" must {
    "have the correct auditType" in {
      fullAuditModel.auditType mustBe "PrePopulated"
    }
    "have the correct detail" when {
      "the pre-pop data is complete and all values present" in {
        fullAuditModel.detail mustBe Json.obj(
          "userType" -> "agent",
          "nino" -> nino,
          "agentReferenceNumber" -> agentReferenceNumber,
          "incomeSources" -> Json.obj(
            "selfEmployments" -> Json.arr(
              Json.obj(
                "name" -> "ABC",
                "description" -> "Plumbing",
                "addressFirstLine" -> "1 long road",
                "addressPostcode" -> "ZZ1 1ZZ",
                "startDate" -> "1980-12-12",
                "accountingMethod" -> "cash"
              )
            ),
            "ukProperty" -> Json.obj(
              "accountingMethod" -> "cash"
            ),
            "overseasProperty" -> Json.obj(
              "accountingMethod" -> "accruals"
            )
          )
        )
      }
      "the pre-pop data is missing all possible values" in {
        minimalAuditModel.detail mustBe Json.obj(
          "userType" -> "individual",
          "nino" -> nino,
          "incomeSources" -> Json.obj()
        )
      }
    }
  }

  def validatePrePopAudit(model: PrePopAuditModel)(expectedJson: JsObject): Unit = {
    model.auditType mustBe "PrePopulated"
  }

}
