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
import models.ErrorModel

class RegistrationSuccessAuditSpec extends CommonSpec {

  "RegistrationSuccessAudit.sanitiseErrorReason" should {
    "strip parser metadata from a coded HIP error" in {
      val rawReason = "API #5317: ITSA Sign Up, Status: 422, Code: 820, Reason: CUSTOMER ALREADY SIGNED UP"

      RegistrationSuccessAudit.sanitiseErrorReason(rawReason) shouldBe "Status: 422, Code: 820, Reason: CUSTOMER ALREADY SIGNED UP"
    }

    "strip parser metadata from a message-only HIP error" in {
      val rawReason = "API #5317: ITSA Sign Up, Status: 500, Message: Unexpected status returned"

      RegistrationSuccessAudit.sanitiseErrorReason(rawReason) shouldBe "Status: 500, Message: Unexpected status returned"
    }

    "leave plain reasons unchanged" in {
      val rawReason = "Failure"

      RegistrationSuccessAudit.sanitiseErrorReason(rawReason) shouldBe "Failure"
    }
  }

  "RegistrationSuccessAudit.detail" should {
    "include the sanitised error reason when the submission audit update is enabled" in {
      val audit = RegistrationSuccessAudit(
        agentReferenceNumber = None,
        nino = "AA123456A",
        utr = "1234567890",
        postSignUpResponse = Left(ErrorModel(
          status = 422,
          code = Some("820"),
          reason = "API #5317: ITSA Sign Up, Status: 422, Code: 820, Reason: CUSTOMER ALREADY SIGNED UP"
        )),
        authToken = "auth",
        path = None,
        submissionAuditUpdateEnabled = true
      )

      audit.detail("errorReason") shouldBe "Status: 422, Code: 820, Reason: CUSTOMER ALREADY SIGNED UP"
    }
  }
}


