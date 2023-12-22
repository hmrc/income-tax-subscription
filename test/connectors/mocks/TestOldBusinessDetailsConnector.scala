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

package connectors.mocks

import config.AppConfig
import connectors.{OldBusinessDetailsConnector, OldGetBusinessDetailsUtil}
import models.ErrorModel
import models.registration.OldGetBusinessDetailsSuccessResponseModel
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status._
import play.api.libs.json.JsValue
import services.mocks.monitoring.MockAuditService
import uk.gov.hmrc.http.{HeaderNames, HttpClient}
import utils.TestConstants._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait TestOldBusinessDetailsConnector extends MockHttp with GuiceOneAppPerSuite with MockAuditService {

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val httpClient: HttpClient = mockHttpClient

  object TestBusinessDetailsConnector extends OldBusinessDetailsConnector(appConfig, httpClient, mockAuditService)

  type TestHttpResponse = (Int, JsValue)

  val getBusinessDetailsSuccess: TestHttpResponse =
    (OK, GetBusinessDetailsResponse.successResponse(testNino, testSafeId, testMtditId))
  val getBusinessDetailsNotFound: TestHttpResponse =
    (NOT_FOUND, GetBusinessDetailsResponse.failureResponse("NOT_FOUND_NINO", "The remote endpoint has indicated that no data can be found"))
  val getBusinessDetailsBadRequest: TestHttpResponse =
    (BAD_REQUEST, GetBusinessDetailsResponse.failureResponse("INVALID_NINO", "Submission has not passed validation. Invalid parameter NINO."))
  val getBusinessDetailsServerError: TestHttpResponse = (
    INTERNAL_SERVER_ERROR,
    GetBusinessDetailsResponse.failureResponse("SERVER_ERROR", "DES is currently experiencing problems that require live service intervention")
  )
  val getBusinessDetailsServiceUnavailable: TestHttpResponse =
    (SERVICE_UNAVAILABLE, GetBusinessDetailsResponse.failureResponse("SERVICE_UNAVAILABLE", "Dependent systems are currently not responding"))

  def mockBusinessDetails(testHttpResponse: TestHttpResponse): Unit = {
    val headers = Seq(
      HeaderNames.authorisation -> appConfig.desAuthorisationToken,
      appConfig.desEnvironmentHeader
    )
    setupMockHttpGet(
      url = Some(TestBusinessDetailsConnector.getBusinessDetailsUrl(testNino)),
      headers = Some(headers)
    )(testHttpResponse._1, testHttpResponse._2)
  }
}

trait MockOldBusinessDetailsConnector extends MockitoSugar {

  val mockOldBusinessDetailsConnector: OldBusinessDetailsConnector = mock[OldBusinessDetailsConnector]

  private def setupMockBusinessDetails(nino: String)(response: Future[OldGetBusinessDetailsUtil.Response]): Unit =
    when(mockOldBusinessDetailsConnector.getBusinessDetails(ArgumentMatchers.eq(nino))(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(response)

  def mockGetBusinessDetailsSuccess(nino: String): Unit =
    setupMockBusinessDetails(nino)(Future.successful(Right(OldGetBusinessDetailsSuccessResponseModel(testMtditId))))

  def mockGetBusinessDetailsNotFound(nino: String): Unit =
    setupMockBusinessDetails(nino)(
      Future.successful(Left(ErrorModel(NOT_FOUND, "NOT_FOUND_NINO", "The remote endpoint has indicated that no data can be found")))
    )

  def mockGetBusinessDetailsFailure(nino: String): Unit =
    setupMockBusinessDetails(nino)(
      Future.successful(Left(ErrorModel(INTERNAL_SERVER_ERROR,
        "SERVER_ERROR",
        "DES is currently experiencing problems that require live service intervention"))))

}