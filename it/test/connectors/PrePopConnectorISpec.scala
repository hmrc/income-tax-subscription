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
import helpers.servicemocks.PrePopStub
import models.subscription.business.Cash
import models.{ErrorModel, PrePopData, PrePopSelfEmployment}
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.mvc.Request
import play.api.test.FakeRequest

class PrePopConnectorISpec extends ComponentSpecBase {

  private lazy val prePopConnector: PrePopConnector = app.injector.instanceOf[PrePopConnector]
  private lazy val appConfig: MicroserviceAppConfig = app.injector.instanceOf[MicroserviceAppConfig]
  implicit val request: Request[_] = FakeRequest()

  "the pre pop connector" when {
    "receiving a OK (200) response" should {
      "return pre pop data" in {
        PrePopStub.stubPrePop(testNino)(
          appConfig.prePopAuthorisationToken,
          appConfig.prePopEnvironment
        )(
          status = OK,
          body = Json.obj(
            "selfEmployment" -> Json.arr(
              Json.obj(
                "businessName" -> "ABC",
                "businessDescription" -> "Plumbing",
                "accountingMethod" -> "C"
              )
            )
          )
        )

        val result = prePopConnector.getPrePopData(testNino)

        result.futureValue shouldBe Right(PrePopData(
          selfEmployment = Some(Seq(
            PrePopSelfEmployment(name = Some("ABC"), trade = Some("Plumbing"), address = None, startDate = None, accountingMethod = Some(Cash))
          )),
          ukPropertyAccountingMethod = None,
          foreignPropertyAccountingMethod = None
        ))
      }
      "return a json parse failure when the received json could not be parsed" in {
        PrePopStub.stubPrePop(testNino)(
          appConfig.prePopAuthorisationToken,
          appConfig.prePopEnvironment
        )(
          status = OK,
          body = Json.obj(
            "selfEmployment" -> Json.arr(
              Json.obj()
            )
          )
        )

        val result = prePopConnector.getPrePopData(testNino)

        result.futureValue shouldBe Left(ErrorModel(OK, "Failure parsing json response from prepop api"))
      }
    }
    "receiving a NOT_FOUND (404) response" should {
      "return an empty pre-pop data" in {
        PrePopStub.stubPrePop(testNino)(
          appConfig.prePopAuthorisationToken,
          appConfig.prePopEnvironment
        )(
          status = NOT_FOUND,
          body = Json.obj()
        )

        val result = prePopConnector.getPrePopData(testNino)

        result.futureValue shouldBe Right(PrePopData(
          selfEmployment = None, ukPropertyAccountingMethod = None, foreignPropertyAccountingMethod = None
        ))
      }
    }
    "receiving a SERVICE_UNAVAILABLE (503) response" should {
      "return an empty pre-pop data" in {
        PrePopStub.stubPrePop(testNino)(
          appConfig.prePopAuthorisationToken,
          appConfig.prePopEnvironment
        )(
          status = SERVICE_UNAVAILABLE,
          body = Json.obj()
        )

        val result = prePopConnector.getPrePopData(testNino)

        result.futureValue shouldBe Right(PrePopData(
          selfEmployment = None, ukPropertyAccountingMethod = None, foreignPropertyAccountingMethod = None
        ))
      }
    }
    "receiving a non handled status response" should {
      "return an unexpected status error" in {
        PrePopStub.stubPrePop(testNino)(
          appConfig.prePopAuthorisationToken,
          appConfig.prePopEnvironment
        )(
          status = INTERNAL_SERVER_ERROR,
          body = Json.obj()
        )

        val result = prePopConnector.getPrePopData(testNino)

        result.futureValue shouldBe Left(ErrorModel(INTERNAL_SERVER_ERROR, "Unexpected status returned from pre-pop api"))
      }
    }
  }
}

