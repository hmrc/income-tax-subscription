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

package parsers

import common.CommonSpec
import models.ErrorModel
import models.status.ITSAStatus.MTDVoluntary
import models.status.{ItsaStatusResponse, TaxYearStatus}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse

class ItsaStatusParserSpec extends CommonSpec {

  private val expectedResponse =
    List(
      TaxYearStatus("2022-23", MTDVoluntary),
      TaxYearStatus("2023-24", MTDVoluntary)
    )

  "ItsaStatusParser" should {
    "return ItsaStatusResponse" when {
      "supplied with an OK response and valid JSON" in {
        val response = HttpResponse(
          OK,
          body = Json.toJson(expectedResponse).toString()
        )

        ItsaStatusParser.ImplicitItsaStatusResponseHttpReads.read("POST", "test-url", response) shouldBe
          Right(ItsaStatusResponse(expectedResponse))
      }
    }

    "return an error" when {
      "supplied with an OK response and invalid JSON" in {
        val response = HttpResponse(OK, body =
          """
            |[
            | { "invalid" : "json" }
            |]
          """.stripMargin)

        ItsaStatusParser.ImplicitItsaStatusResponseHttpReads.read("POST", "test-url", response) shouldBe
          Left(ErrorModel(OK, "Invalid Json for ItsaStatusResponseHttpReads"))
      }

      "supplied with a failed response" in {
        val response = HttpResponse(INTERNAL_SERVER_ERROR, body = "Error body")

        ItsaStatusParser.ImplicitItsaStatusResponseHttpReads.read("POST", "test-url", response) shouldBe
          Left(ErrorModel(INTERNAL_SERVER_ERROR, "Invalid status received"))
      }
    }
  }
}
