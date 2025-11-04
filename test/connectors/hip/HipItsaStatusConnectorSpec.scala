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

package connectors.hip

import common.CommonSpec
import connectors.mocks.MockHttp
import models.ErrorModel
import models.status.ITSAStatus.MTDVoluntary
import models.subscription.AccountingPeriodUtil
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import parsers.GetITSAStatusParser.{GetITSAStatusTaxYearResponse, ITSAStatusDetail}
import parsers.hip.GetITSAStatusParser.GetITSAStatusHttpReads
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse

class HipItsaStatusConnectorSpec extends CommonSpec with MockHttp with GuiceOneAppPerSuite {

  "getItsaStatus" should {
    "retrieve iTSA status" when {
      "the HIP API #5197 returns a successful response" in {
        val data = Seq(GetITSAStatusTaxYearResponse(
          taxYear = AccountingPeriodUtil.getCurrentTaxYear.toShortTaxYear,
          itsaStatusDetails = Seq(ITSAStatusDetail(MTDVoluntary))
        ))

        val response = HttpResponse(
          OK,
          Json.toJson(data).toString
        )

        GetITSAStatusHttpReads.read(response) shouldBe
          Right(data)
      }

      "the HIP API #5197 returns invalid Json" in {
        val response = HttpResponse(
          OK,
          Json.obj().toString
        )

        GetITSAStatusHttpReads.read(response) shouldBe
          Left(ErrorModel(OK, "Failure parsing json response from itsa status api"))
      }

      "the HIP API #5197 returns unexpected status" in {
        val response = HttpResponse(
          INTERNAL_SERVER_ERROR,
          Json.obj().toString
        )

        GetITSAStatusHttpReads.read(response) shouldBe
          Left(ErrorModel(INTERNAL_SERVER_ERROR, "Unexpected status returned from itsa status api"))
      }
    }
  }
}
