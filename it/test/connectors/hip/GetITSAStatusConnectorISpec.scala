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

package connectors.hip

import helpers.WiremockHelper.StubResponse
import helpers.{ComponentSpecBase, WiremockHelper}
import helpers.servicemocks.hip.GetITSAStatusStub
import models.ErrorModel
import models.status.GetITSAStatus
import models.subscription.AccountingPeriodUtil
import parsers.GetITSAStatusParser.{GetITSAStatusTaxYearResponse, ITSAStatusDetail}
import play.api.http.Status.{BAD_GATEWAY, INTERNAL_SERVER_ERROR, NOT_FOUND, OK, SERVICE_UNAVAILABLE, TOO_MANY_REQUESTS}
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}

class GetITSAStatusConnectorISpec extends ComponentSpecBase {

  private lazy val connector: GetITSAStatusConnector = app.injector.instanceOf[GetITSAStatusConnector]

  val testNino: String = "test-nino"

  val currentTaxYear: String = AccountingPeriodUtil.getCurrentTaxYear.toItsaStatusShortTaxYear

  def taxYearResponseJson(status: String): JsValue = Json.obj(
    "taxYear" -> currentTaxYear,
    "itsaStatusDetails" -> Json.arr(
      Json.obj(
        "status" -> status
      )
    )
  )

  def taxYearResponse(status: GetITSAStatus): GetITSAStatusTaxYearResponse = {
    GetITSAStatusTaxYearResponse(
      taxYear = currentTaxYear,
      itsaStatusDetails = Seq(
        ITSAStatusDetail(status)
      )
    )
  }

  "getItsaStatus" when {
    "an OK response is returned with valid json" should {
      "return the parsed sequence of tax year status responses" in {
        GetITSAStatusStub.getITSAStatusStub(testNino)(
          status = OK,
          body = Json.arr(
            taxYearResponseJson("00"),
            taxYearResponseJson("01"),
            taxYearResponseJson("02"),
            taxYearResponseJson("03"),
            taxYearResponseJson("04"),
            taxYearResponseJson("05"),
            taxYearResponseJson("99")
          )
        )

        val result = connector.getItsaStatus(testNino)

        result.futureValue shouldBe Right(Some(Seq(
          taxYearResponse(GetITSAStatus.NoStatus),
          taxYearResponse(GetITSAStatus.MTDMandated),
          taxYearResponse(GetITSAStatus.MTDVoluntary),
          taxYearResponse(GetITSAStatus.Annual),
          taxYearResponse(GetITSAStatus.DigitallyExempt),
          taxYearResponse(GetITSAStatus.Dormant),
          taxYearResponse(GetITSAStatus.MTDExempt)
        )))
      }
    }
    "an OK response is returned with invalid json" should {
      "throw an exception with the error details" in {
        GetITSAStatusStub.getITSAStatusStub(testNino)(
          status = OK,
          body = Json.obj()
        )

        val result = connector.getItsaStatus(testNino)

        result.futureValue shouldBe
          Left(ErrorModel(OK, "API #5197: Get ITSA Status, Status: 200, Message: Failure parsing json response"))
      }
    }
    "a NOT FOUND response is returned" should {
      "return none" in {
        GetITSAStatusStub.getITSAStatusStub(testNino)(
          status = NOT_FOUND,
          body = Json.obj()
        )

        val result = connector.getItsaStatus(testNino)

        result.futureValue shouldBe Right(None)
      }
    }
    "an unexpected status is returned" should {
      "throw an exception with the status received" in {
        GetITSAStatusStub.getITSAStatusStub(testNino)(
          status = INTERNAL_SERVER_ERROR,
          body = Json.obj()
        )

        val result = connector.getItsaStatus(testNino)

        result.futureValue shouldBe
          Left(ErrorModel(INTERNAL_SERVER_ERROR, "API #5197: Get ITSA Status, Status: 500, Message: Unexpected status returned"))
      }
    }

    "retry 3 times" should {
      Seq(TOO_MANY_REQUESTS, BAD_GATEWAY, SERVICE_UNAVAILABLE).foreach { status =>
        s"For a return status of $status" in {
          val url = s"/itsd/person-itd/itsa-status/$testNino\\?taxYear=${AccountingPeriodUtil.getCurrentTaxYear.toShortTaxYear}"
          WiremockHelper.stubGetSequence(url)(
            StubResponse(status),
            StubResponse(status),
            StubResponse(OK, Json.arr(taxYearResponseJson("00")))
          )

          await(connector.getItsaStatus(testNino))

          WiremockHelper.verifyGet(
            uri = url,
            times = 3
          )
        }
      }
    }
  }
}
