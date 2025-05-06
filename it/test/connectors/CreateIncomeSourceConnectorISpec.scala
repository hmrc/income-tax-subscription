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
import helpers.servicemocks.CreateIncomeSourceStub
import models.subscription.CreateIncomeSourcesModel
import models.subscription.business.{CreateIncomeSourceErrorModel, CreateIncomeSourceSuccessModel}
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.mvc.Request
import play.api.test.FakeRequest

class CreateIncomeSourceConnectorISpec extends ComponentSpecBase {

  private lazy val createIncomeSourceConnector: CreateIncomeSourcesConnector = app.injector.instanceOf[CreateIncomeSourcesConnector]
  private lazy val appConfig: MicroserviceAppConfig = app.injector.instanceOf[MicroserviceAppConfig]
  implicit val request: Request[_] = FakeRequest()

  "The income source connector" when {

    "receiving a 200 response" should {

      "return a valid response when valid json is found" in {
        CreateIncomeSourceStub.stub(testMtdbsaRef,
          Json.toJson(testCreateIncomeSources)(CreateIncomeSourcesModel.desWrites),
          appConfig.desAuthorisationToken,
          appConfig.desEnvironment)(
          OK, testCreateIncomeSuccessBody
        )

        val result = createIncomeSourceConnector.createBusinessIncomeSources(Some(testArn), testMtdbsaRef, testCreateIncomeSources)

        result.futureValue shouldBe Right(CreateIncomeSourceSuccessModel())
      }

    }

    "receiving a non-200 response" should {

      "return an error response" in {
        CreateIncomeSourceStub.stub(testMtdbsaRef,
          Json.toJson(testCreateIncomeSources)(CreateIncomeSourcesModel.desWrites),
          appConfig.desAuthorisationToken,
          appConfig.desEnvironment)(
          INTERNAL_SERVER_ERROR, testCreateIncomeFailureBody
        )

        val result = createIncomeSourceConnector.createBusinessIncomeSources(Some(testArn), testMtdbsaRef, testCreateIncomeSources)

        result.futureValue shouldBe Left(CreateIncomeSourceErrorModel(INTERNAL_SERVER_ERROR, testCreateIncomeFailureBody.toString()))
      }
    }
  }
}
