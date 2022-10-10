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

import helpers.ComponentSpecBase
import helpers.servicemocks.GetItsaStatusStub
import models.ErrorModel
import models.status.MtdMandationStatus.Voluntary
import models.status.{ItsaStatusResponse, TaxYearStatus}
import models.subscription.AccountingPeriodUtil
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import play.api.mvc.Request
import play.api.test.FakeRequest

class ItsaStatusConnectorISpec extends ComponentSpecBase {
  private lazy val connector: ItsaStatusConnector = app.injector.instanceOf[ItsaStatusConnector]
  implicit val request: Request[_] = FakeRequest()

  "ItsaStatusConnector" should {
    "return ItsaStatusResponse" when {
      "the status determination service returns OK and a valid JSON response body" in {
        val expectedResponse =
          List(
            TaxYearStatus("2022-23", Voluntary),
            TaxYearStatus("2023-24", Voluntary)
          )

        GetItsaStatusStub.stub(
          "test-nino", "test-utr", AccountingPeriodUtil.getCurrentTaxYear.toItsaStatusShortTaxYear
        )(OK, Json.toJson(expectedResponse))

        val result = connector.getItsaStatus("test-nino", "test-utr")

        result.futureValue shouldBe Right(ItsaStatusResponse(expectedResponse))
      }
    }

    "return an exception" when {
      "the status determination service returns OK and an invalid JSON response body" in {
        GetItsaStatusStub.stubInvalidResponse(
          "test-nino", "test-utr", AccountingPeriodUtil.getCurrentTaxYear.toItsaStatusShortTaxYear
        )(OK, "{}")

        val result = connector.getItsaStatus("test-nino", "test-utr")

        result.futureValue shouldBe Left(ErrorModel(OK, "Invalid Json for itsaStatusResponseHttpReads: List((,List(JsonValidationError(List(error.expected.jsarray),ArraySeq()))))"))
      }
    }

    "return the status and error received" when {
      "the status determination service returns a failure" in {
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

        val result = connector.getItsaStatus("test-nino", "test-utr")

        result.futureValue shouldBe Left(ErrorModel(
          INTERNAL_SERVER_ERROR,
          """{"failures":[{"code":"INVALID_TAX_YEAR","reason":"Submission has not passed validation. Invalid parameter taxYear."}]}"""
        ))
      }
    }
  }
}
