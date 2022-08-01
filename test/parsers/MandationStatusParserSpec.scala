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

package parsers

import common.CommonSpec
import models.ErrorModel
import models.status.MandationStatus.Voluntary
import models.status.MandationStatusResponse
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse

class MandationStatusParserSpec extends CommonSpec {
  "MandationStatusParser" should {
    "return MandationStatusResponse" when {
      "supplied with an OK response and valid JSON" in {
        val response = HttpResponse(
          OK,
          body = Json.toJson(MandationStatusResponse(currentYearStatus = Voluntary, nextYearStatus = Voluntary)).toString()
        )

        MandationStatusParser.mandationStatusResponseHttpReads.read("POST", "test-url", response) shouldBe
          Right(MandationStatusResponse(currentYearStatus = Voluntary, nextYearStatus = Voluntary))
      }
    }

    "return an error" when {
      "supplied with an OK response and invalid JSON" in {
        val response = HttpResponse(OK, body =
          """
            |{
            | "invalid" : "json"
            |}
          """.stripMargin)

        val expectedError = "Invalid Json for mandationStatusResponseHttpReads: " +
          "List(" +
          "(/currentYearStatus,List(JsonValidationError(List(error.path.missing),WrappedArray()))), " +
          "(/nextYearStatus,List(JsonValidationError(List(error.path.missing),WrappedArray())))" +
          ")"

        MandationStatusParser.mandationStatusResponseHttpReads.read("POST", "test-url", response) shouldBe
          Left(ErrorModel(OK, expectedError))
      }

      "supplied with a failed response" in {
        val response = HttpResponse(INTERNAL_SERVER_ERROR, body = "Error body")

        MandationStatusParser.mandationStatusResponseHttpReads.read("POST", "test-url", response) shouldBe
          Left(ErrorModel(INTERNAL_SERVER_ERROR, "Error body"))
      }
    }
  }
}
