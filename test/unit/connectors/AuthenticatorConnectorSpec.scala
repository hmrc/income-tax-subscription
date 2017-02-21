/*
 * Copyright 2017 HM Revenue & Customs
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

package unit.connectors

import models.authenticator.{RefreshFailure, RefreshSuccessful}
import uk.gov.hmrc.play.http.HeaderCarrier
import unit.connectors.mocks.MockAuthenticatorConnector
import utils.TestConstants.Authenticator.refreshFailure


class AuthenticatorConnectorSpec extends MockAuthenticatorConnector {

  implicit val hc = HeaderCarrier()

  "AuthenticatorConnector.refreshProfile" must {

    "return RefreshSuccessful when successful" in {
      mockRefreshProfile(refreshSuccess)

      val result = TestAuthenticatorConnector.refreshProfile
      val enrolResponse = await(result)
      enrolResponse shouldBe RefreshSuccessful
    }

    "return RefreshSuccessful in case of failure" in {
      mockRefreshProfile(refreshFailure)

      val result = TestAuthenticatorConnector.refreshProfile
      val enrolResponse = await(result)
      enrolResponse shouldBe RefreshFailure
    }

  }

}