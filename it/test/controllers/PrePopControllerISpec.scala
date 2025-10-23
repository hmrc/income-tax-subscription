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

package controllers

import config.MicroserviceAppConfig
import config.featureswitch.FeatureSwitching
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.{AuditStub, AuthStub, PrePopStub}
import play.api.http.Status._
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.libs.json.{JsObject, Json}

class PrePopControllerISpec extends ComponentSpecBase with FeatureSwitching {

  val appConfig: MicroserviceAppConfig = app.injector.instanceOf[MicroserviceAppConfig]

  override def overriddenConfig(): Map[String, String] = Map(
    "auditing.enabled" -> "true"
  )

  val hipJson: JsObject = Json.obj(
    "selfEmp" -> Json.arr(
      Json.obj(
        "businessName" -> "ABC",
        "businessDescription" -> "Plumbing"
      )
    )
  )

  val writeJson: JsObject = Json.obj(
    "selfEmployment" -> Json.arr(
      Json.obj(
        "name" -> "ABC",
        "trade" -> "Plumbing"
      )
    )
  )

  "PrePopController" should {
    "return a OK response with pre-pop data" in {
      AuthStub.stubAuth(OK)

      PrePopStub.stubHipPrePop(testNino)(
        appConfig.hipPrePopAuthorisationToken,
      )(
        status = OK,
        body = hipJson
      )

      val res = IncomeTaxSubscription.getPrePop(testNino)

      val expectedJson = writeJson

      res should have(
        httpStatus(OK),
        jsonBodyOf(expectedJson)
      )
      
    }

    "return an INTERNAL_SERVER_ERROR" when {
      "there was a problem with the pre-pop json from the API" in {
        AuthStub.stubAuth(OK)

        val errorJson = Json.obj(
          "selfEmp" -> Json.arr(
            Json.obj(
              "dateBusinessStarted" -> "invalid"
            )
          )
        )

        PrePopStub.stubHipPrePop(testNino)(
          appConfig.hipPrePopAuthorisationToken,
        )(
          status = OK,
          body = errorJson
        )


        val res = IncomeTaxSubscription.getPrePop(testNino)

        res should have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )

      }
      "there was an error returned from the pre-pop API" in {
        AuthStub.stubAuth(OK)

        PrePopStub.stubHipPrePop(testNino)(
          appConfig.hipPrePopAuthorisationToken,
        )(
          status = INTERNAL_SERVER_ERROR,
          body = Json.obj()
        )

        val res = IncomeTaxSubscription.getPrePop(testNino)

        res should have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }
  }
}