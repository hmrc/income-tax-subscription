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

package models.monitoring

import common.CommonSpec

class CreateIncomeSourcesAuditSpec extends CommonSpec {

  "CreateIncomeSourcesAudit.sanitiseErrorReason" should {
    "strip parser metadata from a coded HIP error" in {
      val rawReason = "API #5265: Create income sources, Status: 422, Code: 999, Reason: Request could not be processed"

      CreateIncomeSourcesAudit.sanitiseErrorReason(rawReason) shouldBe "Status: 422, Code: 999, Reason: Request could not be processed"
    }
  }

  "CreateIncomeSourcesAudit.detail" should {
    "include the sanitised error reason on failure" in {
      val audit = CreateIncomeSourcesAudit(
        agentReferenceNumber   = None,
        nino                   = "AA123456A",
        mtdItsaReferenceNumber = "XAIT000000000",
        isSuccess              = false,
        errorReason            = Some("API #5265: Create income sources, Status: 422, Code: 999, Reason: Request could not be processed")
      )

      (audit.detail \ "errorReason").as[String] shouldBe "Status: 422, Code: 999, Reason: Request could not be processed"
    }
  }
}

