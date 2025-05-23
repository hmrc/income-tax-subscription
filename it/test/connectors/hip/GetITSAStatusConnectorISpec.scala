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
import models.status.ITSAStatus.{MTDMandated, MTDVoluntary}
import models.subscription.AccountingPeriodUtil
import parsers.GetITSAStatusParser.{GetITSAStatusTaxYearResponse, ITSAStatusDetail}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.{Json, JsonValidationError, __}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.InternalServerException

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

        result.futureValue shouldBe Seq(
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
        )
      }
    }
    "a successful response is returned with invalid json" should {
      "throw an exception with the error details" in {
        GetITSAStatusStub.stub(testUtr)(
          status = OK,
          body = Json.obj()
        )

        val result = connector.getItsaStatus(testUtr)

        intercept[InternalServerException](await(result))
          .message shouldBe s"[GetITSAStatusParser] - Failure parsing json. Errors: ${Seq(__ -> Seq(JsonValidationError("error.expected.jsarray")))}"
      }
    }
    "an unexpected status is returned" should {
      "throw an exception with the status received" in {
        GetITSAStatusStub.stub(testUtr)(
          status = INTERNAL_SERVER_ERROR,
          body = Json.obj()
        )

        val result = connector.getItsaStatus(testUtr)

        intercept[InternalServerException](await(result))
          .message shouldBe "[GetITSAStatusParser] - Unsupported status received: 500"
      }
    }
  }

}
