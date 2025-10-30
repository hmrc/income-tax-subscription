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

package connectors

import common.CommonSpec
import connectors.mocks.MockHttp
import models.ErrorModel
import models.status.ITSAStatus.MTDVoluntary
import models.status.{ItsaStatusResponse, TaxYearStatus}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import parsers.ItsaStatusParser.ItsaStatusResponseHttpReads
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse

class ItsaStatusConnectorSpec extends CommonSpec with MockHttp with GuiceOneAppPerSuite {

  "getItsaStatus" should {
    "retrieve the user ITSA status" when {
      "the status-determination-service returns a successful response" in {
        val expectedResponse = ItsaStatusResponse(
          List(
            TaxYearStatus("2022-23", MTDVoluntary),
            TaxYearStatus("2023-24", MTDVoluntary)
          )
        )

        val response = HttpResponse(
          OK,
          Json.toJson(expectedResponse.taxYearStatus).toString
        )

        ItsaStatusResponseHttpReads.read("", "", response) shouldBe
          Right(expectedResponse)
      }
    }

    "return an error" when {
      "the status-determination-service returns invalid Json" in {
        val response = HttpResponse(
          OK,
          Json.obj().toString
        )

        ItsaStatusResponseHttpReads.read("", "", response) shouldBe
          Left(ErrorModel(OK, "Invalid Json for ItsaStatusResponseHttpReads"))
      }
      "the status-determination-service returns an invalid status" in {
        val response = HttpResponse(
          INTERNAL_SERVER_ERROR,
          Json.obj().toString
        )

        ItsaStatusResponseHttpReads.read("", "", response) shouldBe
          Left(ErrorModel(INTERNAL_SERVER_ERROR, "Invalid status received"))
      }
    }
  }
}
