/*
 * Copyright 2025 HM Revenue & Customs
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

package connectors.hip

import helpers.WiremockHelper.StubResponse
import helpers.{ComponentSpecBase, WiremockHelper}
import models.ErrorModel
import models.status.{ITSAStatus, ItsaStatusResponse, TaxYearStatus}
import models.subscription.AccountingPeriodUtil
import play.api.http.Status.{BAD_GATEWAY, INTERNAL_SERVER_ERROR, OK, SERVICE_UNAVAILABLE, TOO_MANY_REQUESTS}
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}

class HipItsaStatusConnectorISpec extends ComponentSpecBase {

  private lazy val connector: HipItsaStatusConnector = app.injector.instanceOf[HipItsaStatusConnector]

  val testNino: String = "test-nino"
  val testUtr: String = "test-utr"

  val currentTaxYear: String = AccountingPeriodUtil.getCurrentTaxYear.toItsaStatusShortTaxYear

  val testUrl: String = s"/itsd/itsa-status/signup/$testNino/$testUtr/$currentTaxYear"

  def taxYearStatusJson(status: String): JsObject = Json.obj(
    "taxYear" -> currentTaxYear,
    "status" -> status
  )

  "determineItsaStatus" when {
    "an OK response is returned with valid json" should {
      "return the parsed tax year status response" in {
        WiremockHelper.stubGetSequence(testUrl)(
          StubResponse(OK, Json.arr(
            taxYearStatusJson("01"),
            taxYearStatusJson("02")
          ))
        )

        val result = connector.determineItsaStatus(testNino, testUtr)

        result.futureValue shouldBe Right(ItsaStatusResponse(List(
          TaxYearStatus(currentTaxYear, ITSAStatus.MTDMandated),
          TaxYearStatus(currentTaxYear, ITSAStatus.MTDVoluntary)
        )))
      }
    }

    "an OK response is returned with invalid json" should {
      "throw an exception with the error details" in {
        WiremockHelper.stubGetSequence(testUrl)(
          StubResponse(OK, Json.obj())
        )

        val result = connector.determineItsaStatus(testNino, testUtr)

        result.futureValue shouldBe
          Left(ErrorModel(OK, "API #5197: Determine ITSA Status for Sign Up, Status: 200, Message: Failure parsing json response"))
      }
    }

    "an unexpected status is returned" should {
      "throw an exception with the status received" in {
        WiremockHelper.stubGetSequence(testUrl)(
          StubResponse(INTERNAL_SERVER_ERROR, Json.obj())
        )

        val result = connector.determineItsaStatus(testNino, testUtr)

        result.futureValue shouldBe
          Left(ErrorModel(INTERNAL_SERVER_ERROR, "API #5197: Determine ITSA Status for Sign Up, Status: 500, Message: Unexpected status returned"))
      }
    }

    "retry 3 times" should {
      Seq(TOO_MANY_REQUESTS, BAD_GATEWAY, SERVICE_UNAVAILABLE).foreach { status =>
        s"For a return status of $status" in {
          WiremockHelper.stubGetSequence(testUrl)(
            StubResponse(status),
            StubResponse(status),
            StubResponse(OK, Json.arr(taxYearStatusJson("01")))
          )

          val result = await(connector.determineItsaStatus(testNino, testUtr))

          result shouldBe Right(ItsaStatusResponse(List(
            TaxYearStatus(currentTaxYear, ITSAStatus.MTDMandated)
          )))

          WiremockHelper.verifyGet(
            uri = testUrl,
            times = 3
          )
        }
      }
    }
  }
}