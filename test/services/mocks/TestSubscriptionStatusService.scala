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
import connectors.mocks.MockBusinessDetailsConnector
import models.ErrorModel
import models.frontend.FESuccessResponse
import org.mockito.Mockito._
import org.mockito._
import org.scalatest.mockito.MockitoSugar
import services.SubscriptionStatusService
import utils.TestConstants._

import scala.concurrent.Future

trait TestSubscriptionStatusService extends MockBusinessDetailsConnector {

  val logging = mock[Logging]

  object TestSubscriptionStatusService extends SubscriptionStatusService(mockBusinessDetailsConnector, logging)

}

trait MockSubscriptionStatusService extends MockitoSugar {

  val mockSubscriptionStatusService = mock[SubscriptionStatusService]

  private def mockCheckMtditsaSubscription(nino: String)(response: Future[Either[ErrorModel, Option[FESuccessResponse]]]): Unit =
    when(mockSubscriptionStatusService.checkMtditsaSubscription(ArgumentMatchers.eq(nino))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(response)

  def mockCheckMtditsaFound(nino: String): Unit =
    mockCheckMtditsaSubscription(nino)(Future.successful(Right(Some(FESuccessResponse(Some(testMtditId))))))

  def mockCheckMtditsaNotFound(nino: String): Unit =
    mockCheckMtditsaSubscription(nino)(Future.successful(Right(Some(FESuccessResponse(None)))))

  def mockCheckMtditsaFailure(nino: String): Unit =
    mockCheckMtditsaSubscription(nino)(Future.successful(Left(INVALID_NINO_MODEL)))

}
