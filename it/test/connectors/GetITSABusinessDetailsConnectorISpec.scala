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

package connectors

import config.MicroserviceAppConfig
import config.featureswitch._
import helpers.ComponentSpecBase
import parsers.GetITSABusinessDetailsParser.{AlreadySignedUp, NotSignedUp}
import play.api.http.HeaderNames
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK, UNPROCESSABLE_ENTITY}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import utils.TestConstants.{testMtditId, testNino}

class GetITSABusinessDetailsConnectorISpec extends ComponentSpecBase with FeatureSwitching {

  private lazy val getITSABusinessConnector: GetITSABusinessDetailsConnector = app.injector.instanceOf[GetITSABusinessDetailsConnector]
  lazy val appConfig: MicroserviceAppConfig = app.injector.instanceOf[MicroserviceAppConfig]
  implicit val request: Request[_] = FakeRequest()
  override implicit val hc: HeaderCarrier = HeaderCarrier()

  private def stubGetITSABusinessDetails(nino: String)(status: Int, responseBody: JsValue): Unit = {
    when(
      method = GET,
      uri = s"/etmp/RESTAdapter/itsa/taxpayer/business-details\\?nino=$nino",
      headers = Map(
        HeaderNames.AUTHORIZATION -> "Basic .*",
        "correlationId" -> "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
        "X-Message-Type" -> "TaxpayerDisplay",
        "X-Originating-System" -> "MDTP",
        "X-Receipt-Date" -> """^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z$""",
        "X-Regime-Type" -> "ITSA",
        "X-Transmitting-System" -> "HIP"
      )
    ).thenReturn(status, responseBody)
  }

  def successResponseJson = Json.obj("success" -> Json.obj("taxPayerDisplayResponse" -> Json.obj("mtdId" -> testMtditId)))

  "GetITSABusinessDetailsConnector" should {
    "return AlreadySignedUp when mtdId is present in response" in {
      stubGetITSABusinessDetails(testNino)(OK, successResponseJson)

      val result = getITSABusinessConnector.getHIPBusinessDetails(testNino).futureValue

      result shouldBe AlreadySignedUp(testMtditId)
    }

    "return NotSignedUp when response is UNPROCESSABLE_ENTITY and code 006" in {
      val errorResponse = Json.obj("errors" -> Json.obj("code" -> "006"))
      stubGetITSABusinessDetails(testNino)(UNPROCESSABLE_ENTITY, errorResponse)

      val result = getITSABusinessConnector.getHIPBusinessDetails(testNino).futureValue

      result shouldBe NotSignedUp
    }

    "return NotSignedUp when response is UNPROCESSABLE_ENTITY and code 008" in {
      val errorResponse = Json.obj("errors" -> Json.obj("code" -> "008"))
      stubGetITSABusinessDetails(testNino)(UNPROCESSABLE_ENTITY, errorResponse)

      val result = getITSABusinessConnector.getHIPBusinessDetails(testNino).futureValue

      result shouldBe NotSignedUp
    }

    "throw InternalServerException for unsupported status" in {
      val errorResponse = Json.obj("error" -> "Unsupported status")
      stubGetITSABusinessDetails(testNino)(INTERNAL_SERVER_ERROR, errorResponse)

      val result = getITSABusinessConnector.getHIPBusinessDetails(testNino)

      intercept[InternalServerException](await(result)).getMessage should include("Unsupported status received")
    }

    "throw InternalServerException when mtdId is missing in response" in {
      val badJson = Json.obj("success" -> Json.obj("taxPayerDisplayResponse" -> Json.obj()))
      stubGetITSABusinessDetails(testNino)(OK, badJson)

      val result = getITSABusinessConnector.getHIPBusinessDetails(testNino)

      intercept[InternalServerException](await(result)).getMessage should include("Failure parsing json")
    }

    "throw InternalServerException for BAD_REQUEST response" in {
      val errorResponse = Json.obj("error" -> "Invalid request")
      stubGetITSABusinessDetails(testNino)(BAD_REQUEST, errorResponse)

      val result = getITSABusinessConnector.getHIPBusinessDetails(testNino)

      intercept[InternalServerException](await(result)).getMessage should include("Unsupported status received")
    }

    "throw InternalServerException for INTERNAL_SERVER_ERROR response" in {
      val errorResponse = Json.obj("error" -> "Internal server error")
      stubGetITSABusinessDetails(testNino)(INTERNAL_SERVER_ERROR, errorResponse)

      val result = getITSABusinessConnector.getHIPBusinessDetails(testNino)

      intercept[InternalServerException](await(result)).getMessage should include("Unsupported status received")
    }
  }
}
