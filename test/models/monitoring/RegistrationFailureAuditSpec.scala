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
import org.scalatest.matchers.should.Matchers
import play.api.http.Status._
import utils.Logging._

class RegistrationFailureAuditSpec extends CommonSpec with Matchers {


  "RegistrationFailureAuditSpec" when {
    "an http status code is provided" should {
      "convert BAD_REQUEST to eventTypeBadRequest" in {
        RegistrationFailureAudit.suffix(BAD_REQUEST) should be(eventTypeBadRequest)
      }
      "convert NOT_FOUND to eventTypeNotFound" in {
        RegistrationFailureAudit.suffix(NOT_FOUND) should be(eventTypeNotFound)
      }
      "convert CONFLICT to eventTypeConflict" in {
        RegistrationFailureAudit.suffix(CONFLICT) should be(eventTypeConflict)
      }
      "convert INTERNAL_SERVER_ERROR to eventTypeInternalServerError" in {
        RegistrationFailureAudit.suffix(INTERNAL_SERVER_ERROR) should be(eventTypeInternalServerError)
      }
      "convert SERVICE_UNAVAILABLE to eventTypeServerUnavailable" in {
        RegistrationFailureAudit.suffix(SERVICE_UNAVAILABLE) should be(eventTypeServerUnavailable)
      }
      "convert anything else to eventTypeUnexpectedError" in {
        RegistrationFailureAudit.suffix(-1) should be(eventTypeUnexpectedError)
      }
    }
  }
}
