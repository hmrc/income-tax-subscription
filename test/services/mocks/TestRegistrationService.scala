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

package services.mocks

import audit.Logging
import connectors.NewRegistrationUtil
import connectors.mocks.MockRegistrationConnector
import models.registration.RegistrationSuccessResponseModel
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import services.RegistrationService
import utils.TestConstants.{INVALID_NINO_MODEL, testSafeId}

import scala.concurrent.Future

trait TestRegistrationService extends MockRegistrationConnector {

  val logging = mock[Logging]

  object TestRegistrationService extends RegistrationService(mockRegistrationConnector, logging)

}

trait MockRegistrationService extends MockitoSugar {

  val mockRegistrationService = mock[RegistrationService]

  private def setupMockRegister(nino: String)(response: Future[NewRegistrationUtil.Response]): Unit =
    when(mockRegistrationService.register(ArgumentMatchers.any(), ArgumentMatchers.eq(nino))(ArgumentMatchers.any()))
      .thenReturn(response)

  def mockRegisterSuccess(nino: String): Unit =
    setupMockRegister(nino)(Future.successful(Right(RegistrationSuccessResponseModel(testSafeId))))

  def mockRegisterFailure(nino: String): Unit =
    setupMockRegister(nino)(Future.successful(Left(INVALID_NINO_MODEL)))

}