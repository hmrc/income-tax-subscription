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
import connectors.{GetRegistrationUtil, NewRegistrationUtil, RegistrationConnector}
import models.registration.{RegistrationRequestModel, RegistrationSuccessResponseModel}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status._
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.{HttpGet, HttpPost}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import utils.Implicits._
import utils.TestConstants.{GetRegistrationResponse, NewRegistrationResponse, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait TestRegistrationConnector extends MockHttp with GuiceOneAppPerSuite {

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val logging: Logging = app.injector.instanceOf[Logging]
  lazy val httpPost: HttpPost = mockHttpClient
  lazy val httpClient: HttpClient = mockHttpClient

  def mockRegister(payload: RegistrationRequestModel) = (setupMockRegister(testNino, payload) _).tupled

  val mockGetRegistration = (setupMockGetRegistration(testNino) _).tupled

  object TestRegistrationConnector extends RegistrationConnector(appConfig, logging, httpClient)

  val regSuccess = (OK, NewRegistrationResponse.successResponse(testSafeId))
  val getRegSuccess = (OK, GetRegistrationResponse.successResponse(testSafeId))

  def setupMockRegister(nino: String, payload: RegistrationRequestModel)(status: Int, response: JsValue): Unit =
    setupMockHttpPost(url = TestRegistrationConnector.newRegistrationUrl(nino), payload)(status, response)

  def setupMockGetRegistration(nino: String)(status: Int, response: JsValue): Unit =
    setupMockHttpGet(url = TestRegistrationConnector.getRegistrationUrl(nino))(status, response)
}


trait MockRegistrationConnector extends MockitoSugar {

  val mockRegistrationConnector = mock[RegistrationConnector]

  private def setupMockRegister(nino: String, payload: RegistrationRequestModel)(response: Future[NewRegistrationUtil.Response]): Unit =
    when(mockRegistrationConnector.register(ArgumentMatchers.eq(nino), ArgumentMatchers.eq(payload))(ArgumentMatchers.any()))
      .thenReturn(response)

  def mockRegisterSuccess(nino: String, payload: RegistrationRequestModel): Unit =
    setupMockRegister(nino, payload)(Future.successful(Right(RegistrationSuccessResponseModel(testSafeId))))

  def mockRegisterConflict(nino: String, payload: RegistrationRequestModel): Unit =
    setupMockRegister(nino, payload)(Future.successful(Left(CONFLICT_ERROR_MODEL)))

  def mockRegisterFailure(nino: String, payload: RegistrationRequestModel): Unit =
    setupMockRegister(nino, payload)(Future.successful(Left(INVALID_NINO_MODEL)))

  private def setupMockGetRegistration(nino: String)(response: Future[GetRegistrationUtil.Response]): Unit =
    when(mockRegistrationConnector.getRegistration(ArgumentMatchers.eq(nino))(ArgumentMatchers.any()))
      .thenReturn(response)

  def mockGetRegistrationSuccess(nino: String): Unit =
    setupMockGetRegistration(nino)(Future.successful(Right(RegistrationSuccessResponseModel(testSafeId))))

  def mockGetRegistrationFailure(nino: String): Unit =
    setupMockGetRegistration(nino)(Future.successful(Left(INVALID_NINO_MODEL)))

}