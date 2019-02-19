/*
 * Copyright 2019 HM Revenue & Customs
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

import audit.Logging
import config.AppConfig
import connectors.{BusinessDetailsConnector, GetBusinessDetailsUtil}
import models.ErrorModel
import models.registration.GetBusinessDetailsSuccessResponseModel
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status._
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.{HttpGet, HttpPost}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import utils.Implicits._
import utils.TestConstants._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait TestBusinessDetailsConnector extends MockHttp with GuiceOneAppPerSuite {

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val logging: Logging = app.injector.instanceOf[Logging]
  lazy val httpClient: HttpClient = mockHttpClient


  val mockBusinessDetails = (setupMockBusinessDetails(testNino) _).tupled

  object TestBusinessDetailsConnector extends BusinessDetailsConnector(appConfig, logging, httpClient)

  val getBusinessDetailsSuccess = (OK, GetBusinessDetailsResponse.successResponse(testNino, testSafeId, testMtditId))
  val getBusinessDetailsNotFound = (NOT_FOUND, GetBusinessDetailsResponse.failureResponse("NOT_FOUND_NINO", "The remote endpoint has indicated that no data can be found"))
  val getBusinessDetailsBadRequest = (BAD_REQUEST, GetBusinessDetailsResponse.failureResponse("INVALID_NINO", "Submission has not passed validation. Invalid parameter NINO."))
  val getBusinessDetailsServerError = (INTERNAL_SERVER_ERROR, GetBusinessDetailsResponse.failureResponse("SERVER_ERROR", "DES is currently experiencing problems that require live service intervention"))
  val getBusinessDetailsServiceUnavailable = (SERVICE_UNAVAILABLE, GetBusinessDetailsResponse.failureResponse("SERVICE_UNAVAILABLE", "Dependent systems are currently not responding"))

  def setupMockBusinessDetails(nino: String)(status: Int, response: JsValue): Unit =
    setupMockHttpGet(url = TestBusinessDetailsConnector.getBusinessDetailsUrl(nino))(status, response)
}

trait MockBusinessDetailsConnector extends MockitoSugar {

  val mockBusinessDetailsConnector = mock[BusinessDetailsConnector]

  private def setupMockBusinessDetails(nino: String)(response: Future[GetBusinessDetailsUtil.Response]): Unit =
    when(mockBusinessDetailsConnector.getBusinessDetails(ArgumentMatchers.eq(nino))(ArgumentMatchers.any())).thenReturn(response)

  def mockGetBusinessDetailsSuccess(nino: String): Unit =
    setupMockBusinessDetails(nino)(Future.successful(Right(GetBusinessDetailsSuccessResponseModel(testMtditId))))

  def mockGetBusinessDetailsNotFound(nino: String): Unit =
    setupMockBusinessDetails(nino)(Future.successful(Left(ErrorModel(NOT_FOUND, "NOT_FOUND_NINO", "The remote endpoint has indicated that no data can be found"))))

  def mockGetBusinessDetailsFailure(nino: String): Unit =
    setupMockBusinessDetails(nino)(
      Future.successful(Left(ErrorModel(INTERNAL_SERVER_ERROR,
        "SERVER_ERROR",
        "DES is currently experiencing problems that require live service intervention"))))


}