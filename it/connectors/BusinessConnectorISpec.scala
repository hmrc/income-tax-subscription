/*
 * Copyright 2020 HM Revenue & Customs
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

import config.MicroserviceAppConfig
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.BusinessSubscriptionStub.stubBusinessIncomeSubscription
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.mvc.Request
import play.api.test.FakeRequest
import uk.gov.hmrc.http.InternalServerException

class BusinessConnectorISpec extends ComponentSpecBase {

  private lazy val businessConnector: BusinessConnector = app.injector.instanceOf[BusinessConnector]
  private lazy val appConfig: MicroserviceAppConfig = app.injector.instanceOf[MicroserviceAppConfig]
  implicit val request: Request[_] = FakeRequest()

  "business connector" when {
    "business subscription returns a successful response" should {
      "return businessSubscriptionPayload" in {
        stubBusinessIncomeSubscription(testNino, testBusinessIncomeJson, appConfig.desAuthorisationToken, appConfig.desEnvironment)(
          OK, Json.obj("mtditId" -> testMtditId)
        )

        val res = businessConnector.businessSubscribe(testNino, testBusinessIncomeModel, Some(testArn))

        await(res) shouldBe testMtditId
      }
    }

    "business subscription errors " should {
      "throw an Exception where the nino number doesn't exist" in {
        stubBusinessIncomeSubscription(testNino, testBusinessIncomeJson, appConfig.desAuthorisationToken, appConfig.desEnvironment)(
          INTERNAL_SERVER_ERROR, Json.obj()
        )

        intercept[InternalServerException] {
          val res = businessConnector.businessSubscribe(testNino, testBusinessIncomeModel, Some(testArn))
          await(res)
        }
      }
    }
  }
}
