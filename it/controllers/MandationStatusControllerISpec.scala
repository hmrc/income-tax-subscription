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
import helpers.servicemocks.{AuthStub, GetItsaStatusStub}
import models.status.MtdMandationStatus.Voluntary
import models.status.{MandationStatusRequest, MandationStatusResponse, TaxYearStatus}
import models.subscription.AccountingPeriodUtil
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json

class MandationStatusControllerISpec extends ComponentSpecBase {
  "POST /itsa-status" should {
    "return OK"  when {
      "the status-determination-service returns OK status and valid JSON" in {
        Given("I setup the Wiremock stubs")
        val expectedResponse =
          List(
            TaxYearStatus(AccountingPeriodUtil.getCurrentTaxYear.toItsaStatusShortTaxYear, Voluntary),
            TaxYearStatus(AccountingPeriodUtil.getNextTaxYear.toItsaStatusShortTaxYear, Voluntary)
          )
        AuthStub.stubAuth(OK, Json.obj())
        GetItsaStatusStub.stub(
          "test-nino", "test-utr", AccountingPeriodUtil.getCurrentTaxYear.toItsaStatusShortTaxYear
        )(OK, Json.toJson(expectedResponse))

        When("POST /itsa-status is called")
        val response = IncomeTaxSubscription.mandationStatus(Json.toJson(MandationStatusRequest("test-nino", "test-utr")))

        Then("Should return a OK and the mandation status response")
        response should have(
          httpStatus(OK)
        )
        response should have(
          jsonBodyAs[MandationStatusResponse](MandationStatusResponse(currentYearStatus = Voluntary, nextYearStatus = Voluntary))
        )
      }
    }

    "return BAD_REQUEST" when {
      "the request body is invalid" in {
        Given("I setup the Wiremock stubs")
        AuthStub.stubAuth(OK, Json.obj())

        When("POST /itsa-status is called")
        val response = IncomeTaxSubscription.mandationStatus(Json.obj("invalid" -> "request"))

        Then("Should return a BAD_REQUEST")
        response should have(
          httpStatus(BAD_REQUEST)
        )
      }
    }

    "return INTERNAL_SERVER_ERROR"  when {
      "the status-determination-service returns OK status and invalid JSON" in {
        Given("I setup the Wiremock stubs")
        AuthStub.stubAuth(OK, Json.obj())
        GetItsaStatusStub.stubInvalidResponse(
          "test-nino", "test-utr", AccountingPeriodUtil.getCurrentTaxYear.toItsaStatusShortTaxYear
        )(OK, "{ currentYearStatus")

        When("POST /itsa-status is called")
        val response = IncomeTaxSubscription.mandationStatus(Json.toJson(MandationStatusRequest("test-nino", "test-utr")))

        Then("Should return an INTERNAL_SERVER_ERROR")
        response should have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }

      "the status-determination-service returns INTERNAL_SERVER_ERROR" in {
        Given("I setup the Wiremock stubs")
        AuthStub.stubAuth(OK, Json.obj())
        val failedResponse = Json.obj(
          "failures" -> Json.arr(
            Json.obj(
              "code" -> "INVALID_TAX_YEAR",
              "reason" -> "Submission has not passed validation. Invalid parameter taxYear."
            )
          )
        )
        GetItsaStatusStub.stub(
          "test-nino", "test-utr", AccountingPeriodUtil.getCurrentTaxYear.toItsaStatusShortTaxYear
        )(INTERNAL_SERVER_ERROR, failedResponse)

        When("POST /itsa-status is called")
        val response = IncomeTaxSubscription.mandationStatus(Json.toJson(MandationStatusRequest("test-nino", "test-utr")))

        Then("Should return an INTERNAL_SERVER_ERROR")
        response should have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }
  }
}
