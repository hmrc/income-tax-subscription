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

package connectors

import helpers.ComponentSpecBase
import play.api.http.Status._
import helpers.IntegrationTestConstants._
import helpers.servicemocks.RegistrationStub
import uk.gov.hmrc.http.BadRequestException

class RegistrationConnectorISpec extends ComponentSpecBase {
  val registrationConnector: RegistrationConnector = app.injector.instanceOf[RegistrationConnector]

  "register" when {
    s"the downstream service returns $OK" should {
      "return a successful registration" in {
        val isAnAgent = true

        RegistrationStub.stubRegistration(testNino, isAnAgent)(OK)

        await(registrationConnector.register(testNino, isAnAgent)) shouldBe RegistrationSuccess
      }
    }
    s"the downstream service returns $BAD_REQUEST" should {
      "throw a BadRequestException" in {
        val isAnAgent = true
        RegistrationStub.stubRegistration(testNino, isAnAgent)(BAD_REQUEST)

        intercept[BadRequestException](await(registrationConnector.register(testNino, isAnAgent)))
      }
    }
  }
}
