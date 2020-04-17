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
import helpers.IntegrationTestConstants._
import helpers.servicemocks.RegistrationStub
import models.ErrorModel
import play.api.http.Status._
import play.api.mvc.Request
import play.api.test.FakeRequest

class RegistrationConnectorISpec extends ComponentSpecBase {

  val registrationConnector: RegistrationConnector = app.injector.instanceOf[RegistrationConnector]

  implicit val request: Request[_] = FakeRequest()

  "register" when {
    s"the downstream service returns $OK" should {
      "return a successful registration" in {
        val isAnAgent = true

        RegistrationStub.stubRegistration(testNino, isAnAgent)(OK)

        await(registrationConnector.register(testNino, isAnAgent)) shouldBe Right(RegistrationSuccess)
      }
    }
    s"the downstream service returns anything other than $OK" should {
      "return an error model with the status" in {
        val isAnAgent = true

        RegistrationStub.stubRegistration(testNino, isAnAgent)(INTERNAL_SERVER_ERROR)

        await(registrationConnector.register(testNino, isAnAgent)) shouldBe Left(ErrorModel(INTERNAL_SERVER_ERROR, s"Failed to register $testNino"))
      }
    }
  }
}
