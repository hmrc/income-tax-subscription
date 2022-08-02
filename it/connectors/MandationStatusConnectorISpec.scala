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
import helpers.IntegrationTestConstants.failureResponse
import helpers.servicemocks.GetMandationStatusStub
import models.ErrorModel
import models.status.MandationStatus.Voluntary
import models.status.{MandationStatusRequest, MandationStatusResponse}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import play.api.mvc.Request
import play.api.test.FakeRequest

class MandationStatusConnectorISpec extends ComponentSpecBase {
  private lazy val connector: MandationStatusConnector = app.injector.instanceOf[MandationStatusConnector]
  implicit val request: Request[_] = FakeRequest()

  "MandationStatusConnector" should {
    "return MandationStatusResponse" when {
      "the status determination service returns OK and a valid JSON response body" in {
        GetMandationStatusStub.stub(
          Json.toJson(MandationStatusRequest("test-nino", "test-utr"))
        )(OK, Json.toJson(MandationStatusResponse(currentYearStatus = Voluntary, nextYearStatus = Voluntary)))

        val result = connector.getMandationStatus("test-nino", "test-utr")

        result.futureValue shouldBe Right(MandationStatusResponse(currentYearStatus = Voluntary, nextYearStatus = Voluntary))
      }
    }

    "return an exception" when {
      "the status determination service returns OK and an invalid JSON response body" in {
        GetMandationStatusStub.stubInvalidResponse(
          Json.toJson(MandationStatusRequest("test-nino", "test-utr"))
        )(OK, "{ currentYearStatus")

        val result = connector.getMandationStatus("test-nino", "test-utr")

        result.futureValue shouldBe Left(ErrorModel(OK, "Invalid Json for mandationStatusResponseHttpReads: List((,List(JsonValidationError(List(error.expected.jsobject),WrappedArray()))))"))
      }
    }

    "return the status and error received" when {
      "the status determination service returns a failure" in {
        GetMandationStatusStub.stub(
          Json.toJson(MandationStatusRequest("test-nino", "test-utr"))
        )(INTERNAL_SERVER_ERROR, failureResponse("code", "reason"))

        val result = connector.getMandationStatus("test-nino", "test-utr")

        result.futureValue shouldBe Left(ErrorModel(INTERNAL_SERVER_ERROR, """{"code":"code","reason":"reason"}"""))
      }
    }
  }
}
