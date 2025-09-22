/*
 * Copyright 2022 HM Revenue & Customs
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

import config.AppConfig
import config.featureswitch.{FeatureSwitching, UseHIPForItsaStatus}
import helpers.ComponentSpecBase
import helpers.servicemocks.hip.GetITSAStatusStub
import helpers.servicemocks.{AuthStub, GetItsaStatusStub}
import models.status.ITSAStatus.{MTDMandated, MTDVoluntary}
import models.status.{MandationStatusRequest, MandationStatusResponse}
import models.subscription.{AccountingPeriodModel, AccountingPeriodUtil}
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK, UNAUTHORIZED}
import play.api.libs.json.Json

class MandationStatusControllerISpec extends ComponentSpecBase with FeatureSwitching {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(UseHIPForItsaStatus)
  }

  s"POST ${routes.MandationStatusController.mandationStatus.url}" when {
    "there is no authorisation" should {
      "return UNAUTHORISED" in {
        AuthStub.stubAuthFailure()

        val result = IncomeTaxSubscription.mandationStatus(
          body = Json.toJson(MandationStatusRequest(testNino, testUtr))
        )

        result should have(
          httpStatus(UNAUTHORIZED)
        )
      }
    }

    "the new get itsa status api feature switch is enabled" should {
      "return OK" when {
        "the api call returns OK with valid json" in {
          enable(UseHIPForItsaStatus)

          AuthStub.stubAuthSuccess()
          GetITSAStatusStub.stubHip(testNino, testUtr)(
            status = OK,
            body = Json.arr(
              Json.obj(
                "taxYear" -> currentTaxYear.toItsaStatusShortTaxYear,
                "status" -> "02"
              ),
              Json.obj(
                "taxYear" -> nextTaxYear.toItsaStatusShortTaxYear,
                "status" -> "01"
              )
            )
          )

          val result = IncomeTaxSubscription.mandationStatus(
            body = Json.toJson(MandationStatusRequest(testNino, testUtr))
          )

          result should have(
            httpStatus(OK),
            jsonBodyAs(MandationStatusResponse(MTDVoluntary, MTDMandated))
          )
        }
      }
      "return BAD_REQUEST" when {
        "the request received does not have the correct payload" in {
          enable(UseHIPForItsaStatus)

          AuthStub.stubAuthSuccess()

          val result = IncomeTaxSubscription.mandationStatus(
            body = Json.obj()
          )

          result should have(
            httpStatus(BAD_REQUEST)
          )
        }
      }
      "return INTERNAL_SERVER_ERROR" when {
        "an error occurred calling the API" in {
          enable(UseHIPForItsaStatus)

          AuthStub.stubAuthSuccess()
          GetITSAStatusStub.stub(testUtr)(
            status = INTERNAL_SERVER_ERROR,
            body = Json.arr()
          )

          val result = IncomeTaxSubscription.mandationStatus(
            body = Json.toJson(MandationStatusRequest(testNino, testUtr))
          )

          result should have(
            httpStatus(INTERNAL_SERVER_ERROR)
          )
        }
        "the current tax year could not be found in the api response" in {
          enable(UseHIPForItsaStatus)

          AuthStub.stubAuthSuccess()
          GetITSAStatusStub.stub(testUtr)(
            status = OK,
            body = Json.arr(
              Json.obj(
                "taxYear" -> nextTaxYear.toItsaStatusShortTaxYear,
                "itsaStatusDetails" -> Json.arr(
                  Json.obj(
                    "status" -> "01"
                  )
                )
              )
            )
          )

          val result = IncomeTaxSubscription.mandationStatus(
            body = Json.toJson(MandationStatusRequest(testNino, testUtr))
          )

          result should have(
            httpStatus(INTERNAL_SERVER_ERROR)
          )
        }
        "the current tax year status could not be found in the api response" in {
          enable(UseHIPForItsaStatus)

          AuthStub.stubAuthSuccess()
          GetITSAStatusStub.stub(testUtr)(
            status = OK,
            body = Json.arr(
              Json.obj(
                "taxYear" -> currentTaxYear.toItsaStatusShortTaxYear,
                "itsaStatusDetails" -> Json.arr()
              ),
              Json.obj(
                "taxYear" -> nextTaxYear.toItsaStatusShortTaxYear,
                "itsaStatusDetails" -> Json.arr(
                  Json.obj(
                    "status" -> "01"
                  )
                )
              )
            )
          )

          val result = IncomeTaxSubscription.mandationStatus(
            body = Json.toJson(MandationStatusRequest(testNino, testUtr))
          )

          result should have(
            httpStatus(INTERNAL_SERVER_ERROR)
          )
        }
        "the next tax year could not be found in the api response" in {
          enable(UseHIPForItsaStatus)

          AuthStub.stubAuthSuccess()
          GetITSAStatusStub.stub(testUtr)(
            status = OK,
            body = Json.arr(
              Json.obj(
                "taxYear" -> currentTaxYear.toItsaStatusShortTaxYear,
                "itsaStatusDetails" -> Json.arr(
                  Json.obj(
                    "status" -> "02"
                  )
                )
              )
            )
          )

          val result = IncomeTaxSubscription.mandationStatus(
            body = Json.toJson(MandationStatusRequest(testNino, testUtr))
          )

          result should have(
            httpStatus(INTERNAL_SERVER_ERROR)
          )
        }
        "the next tax year status could not be found in the api response" in {
          enable(UseHIPForItsaStatus)

          AuthStub.stubAuthSuccess()
          GetITSAStatusStub.stub(testUtr)(
            status = OK,
            body = Json.arr(
              Json.obj(
                "taxYear" -> currentTaxYear.toItsaStatusShortTaxYear,
                "itsaStatusDetails" -> Json.arr(
                  Json.obj(
                    "status" -> "02"
                  )
                )
              ),
              Json.obj(
                "taxYear" -> nextTaxYear.toItsaStatusShortTaxYear,
                "itsaStatusDetails" -> Json.arr()
              )
            )
          )

          val result = IncomeTaxSubscription.mandationStatus(
            body = Json.toJson(MandationStatusRequest(testNino, testUtr))
          )

          result should have(
            httpStatus(INTERNAL_SERVER_ERROR)
          )
        }
      }
    }
    "the new get itsa status api feature switch is disabled" should {
      "return OK" when {
        "the api call returns OK with valid json" in {
          AuthStub.stubAuthSuccess()
          GetItsaStatusStub.stub(testNino, testUtr, currentTaxYear.toItsaStatusShortTaxYear)(
            status = OK,
            body = Json.arr(
              Json.obj(
                "taxYear" -> currentTaxYear.toItsaStatusShortTaxYear,
                "status" -> MTDVoluntary.value
              ),
              Json.obj(
                "taxYear" -> nextTaxYear.toItsaStatusShortTaxYear,
                "status" -> MTDMandated.value
              )
            )
          )

          val result = IncomeTaxSubscription.mandationStatus(
            body = Json.toJson(MandationStatusRequest(testNino, testUtr))
          )

          result should have(
            httpStatus(OK),
            jsonBodyAs(MandationStatusResponse(MTDVoluntary, MTDMandated))
          )
        }
      }
      "return BAD_REQUEST" when {
        "the request received does not have the correct payload" in {
          AuthStub.stubAuthSuccess()

          val result = IncomeTaxSubscription.mandationStatus(body = Json.obj())

          result should have(
            httpStatus(BAD_REQUEST)
          )
        }
      }
      "return INTERNAL_SERVER_ERROR" when {
        "an error occurred calling the API" in {
          AuthStub.stubAuthSuccess()
          GetItsaStatusStub.stub(testNino, testUtr, currentTaxYear.toItsaStatusShortTaxYear)(
            status = INTERNAL_SERVER_ERROR,
            body = Json.arr()
          )

          val result = IncomeTaxSubscription.mandationStatus(
            body = Json.toJson(MandationStatusRequest(testNino, testUtr))
          )

          result should have(
            httpStatus(INTERNAL_SERVER_ERROR)
          )
        }
        "the current tax year status could not be found in the api response" in {
          AuthStub.stubAuthSuccess()
          GetItsaStatusStub.stub(testNino, testUtr, currentTaxYear.toItsaStatusShortTaxYear)(
            status = OK,
            body = Json.arr(
              Json.obj(
                "taxYear" -> nextTaxYear.toItsaStatusShortTaxYear,
                "status" -> MTDMandated.value
              )
            )
          )

          val result = IncomeTaxSubscription.mandationStatus(
            body = Json.toJson(MandationStatusRequest(testNino, testUtr))
          )

          result should have(
            httpStatus(INTERNAL_SERVER_ERROR)
          )
        }
        "the next tax year status could not be found in the api response" in {
          AuthStub.stubAuthSuccess()
          GetItsaStatusStub.stub(testNino, testUtr, currentTaxYear.toItsaStatusShortTaxYear)(
            status = OK,
            body = Json.arr(
              Json.obj(
                "taxYear" -> currentTaxYear.toItsaStatusShortTaxYear,
                "status" -> MTDVoluntary.value
              )
            )
          )

          val result = IncomeTaxSubscription.mandationStatus(
            body = Json.toJson(MandationStatusRequest(testNino, testUtr))
          )

          result should have(
            httpStatus(INTERNAL_SERVER_ERROR)
          )
        }
      }
    }
  }

  lazy val testNino: String = "AA000000A"
  lazy val testUtr: String = "1234567890"

  lazy val currentTaxYear: AccountingPeriodModel = AccountingPeriodUtil.getCurrentTaxYear
  lazy val nextTaxYear: AccountingPeriodModel = AccountingPeriodUtil.getNextTaxYear

}
