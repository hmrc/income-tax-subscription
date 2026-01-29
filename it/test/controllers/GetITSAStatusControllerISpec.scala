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

import helpers.ComponentSpecBase
import helpers.servicemocks.AuthStub
import helpers.servicemocks.hip.GetITSAStatusStub
import models.status.GetITSAStatusRequest
import models.subscription.{AccountingPeriodModel, AccountingPeriodUtil}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, UNAUTHORIZED}
import play.api.libs.json.Json

class GetITSAStatusControllerISpec extends ComponentSpecBase {

  s"POST ${routes.GetITSAStatusController.getITSAStatus.url}" when {
    "there is no authorisation" should {
      "return UNAUTHORISED" in {
        AuthStub.stubAuthFailure()

        val result = IncomeTaxSubscription.getITSAStatus(
          body = Json.toJson(GetITSAStatusRequest(testNino))
        )

        result should have(
          httpStatus(UNAUTHORIZED)
        )
      }
    }

    "return OK" when {
      "the api returns a status" in {
        AuthStub.stubAuthSuccess()
        GetITSAStatusStub.getITSAStatusStub(testNino)(
          status = OK,
          body = Json.arr(
            Json.obj(
              "taxYear" -> currentTaxYear.toItsaStatusShortTaxYear,
              "itsaStatusDetails" -> Json.arr(
                Json.obj(
                  "status" -> "00"
                )
              )
            )
          )
        )

        val result = IncomeTaxSubscription.getITSAStatus(
          body = Json.toJson(GetITSAStatusRequest(testNino))
        )

        result should have(
          httpStatus(OK),
          jsonBodyOf(Json.obj(
            "status" -> "No Status"
          ))
        )
      }
      "the api returns multiple status" in {
        AuthStub.stubAuthSuccess()
        GetITSAStatusStub.getITSAStatusStub(testNino)(
          status = OK,
          body = Json.arr(
            Json.obj(
              "taxYear" -> currentTaxYear.toItsaStatusShortTaxYear,
              "itsaStatusDetails" -> Json.arr(
                Json.obj(
                  "status" -> "00"
                ),
                Json.obj(
                  "status" -> "01"
                )
              )
            )
          )
        )

        val result = IncomeTaxSubscription.getITSAStatus(
          body = Json.toJson(GetITSAStatusRequest(testNino))
        )

        result should have(
          httpStatus(OK),
          jsonBodyOf(Json.obj(
            "status" -> "No Status"
          ))
        )
      }
    }
    "return INTERNAL_SERVER_ERROR" when {
      "the api response is an empty array" in {
        AuthStub.stubAuthSuccess()
        GetITSAStatusStub.getITSAStatusStub(testNino)(
          status = OK,
          body = Json.arr()
        )

        val result = IncomeTaxSubscription.getITSAStatus(
          body = Json.toJson(GetITSAStatusRequest(testNino))
        )

        result should have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
      "the api response has a tax year response but no status" in {
        AuthStub.stubAuthSuccess()
        GetITSAStatusStub.getITSAStatusStub(testNino)(
          status = OK,
          body = Json.arr(
            Json.obj(
              "taxYear" -> currentTaxYear.toItsaStatusShortTaxYear,
              "itsaStatusDetails" -> Json.arr()
            )
          )
        )

        val result = IncomeTaxSubscription.getITSAStatus(
          body = Json.toJson(GetITSAStatusRequest(testNino))
        )

        result should have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
      "the api returns an unexpected status" in {
        AuthStub.stubAuthSuccess()
        GetITSAStatusStub.getITSAStatusStub(testNino)(
          status = INTERNAL_SERVER_ERROR,
          body = Json.arr()
        )

        val result = IncomeTaxSubscription.getITSAStatus(
          body = Json.toJson(GetITSAStatusRequest(testNino))
        )

        result should have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }
  }

  lazy val testNino: String = "AA000000A"
  lazy val testUtr: String = "1234567890"

  lazy val currentTaxYear: AccountingPeriodModel = AccountingPeriodUtil.getCurrentTaxYear
  lazy val nextTaxYear: AccountingPeriodModel = AccountingPeriodUtil.getNextTaxYear

}
