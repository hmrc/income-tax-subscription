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

import helpers.ComponentSpecBase
import helpers.servicemocks.hip.GetITSAStatusStub
import models.ErrorModel
import models.status.ITSAStatus.{MTDMandated, MTDVoluntary}
import models.subscription.AccountingPeriodUtil
import parsers.GetITSAStatusParser.{GetITSAStatusTaxYearResponse, ITSAStatusDetail}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json

class GetITSAStatusConnectorISpec extends ComponentSpecBase {

  private lazy val connector: GetITSAStatusConnector = app.injector.instanceOf[GetITSAStatusConnector]

  val testUtr: String = "1234567890"

  val currentTaxYear: String = AccountingPeriodUtil.getCurrentTaxYear.toItsaStatusShortTaxYear
  val nextTaxYear: String = AccountingPeriodUtil.getNextTaxYear.toItsaStatusShortTaxYear

  "getItsaStatus" when {
    "a successful response is returned with valid json" should {
      "return the parsed sequence of tax year status responses" in {
        GetITSAStatusStub.stub(testUtr)(
          status = OK,
          body = Json.arr(
            Json.obj(
              "taxYear" -> currentTaxYear,
              "itsaStatusDetails" -> Json.arr(
                Json.obj(
                  "status" -> "02"
                )
              )
            ),
            Json.obj(
              "taxYear" -> nextTaxYear,
              "itsaStatusDetails" -> Json.arr(
                Json.obj(
                  "status" -> "01"
                )
              )
            )
          )
        )

        val result = connector.getItsaStatus(testUtr)

        result.futureValue shouldBe Right(Seq(
          GetITSAStatusTaxYearResponse(
            taxYear = currentTaxYear,
            itsaStatusDetails = Seq(
              ITSAStatusDetail(MTDVoluntary)
            )
          ),
          GetITSAStatusTaxYearResponse(
            taxYear = nextTaxYear,
            itsaStatusDetails = Seq(
              ITSAStatusDetail(MTDMandated)
            )
          )
        ))
      }
    }
    "a successful response is returned with invalid json" should {
      "throw an exception with the error details" in {
        GetITSAStatusStub.stub(testUtr)(
          status = OK,
          body = Json.obj()
        )

        val result = connector.getItsaStatus(testUtr)

        result.futureValue shouldBe
          Left(ErrorModel(OK, "Failure parsing json response from itsa status api"))
      }
    }
    "an unexpected status is returned" should {
      "throw an exception with the status received" in {
        GetITSAStatusStub.stub(testUtr)(
          status = INTERNAL_SERVER_ERROR,
          body = Json.obj()
        )

        val result = connector.getItsaStatus(testUtr)

        result.futureValue shouldBe
          Left(ErrorModel(INTERNAL_SERVER_ERROR, "Unexpected status returned from itsa status api"))
      }
    }
  }
}
