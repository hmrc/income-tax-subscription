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

package services.mocks

import config.MicroserviceAppConfig
import connectors.mocks.{MockBusinessDetailsConnector, MockOldBusinessDetailsConnector}
import models.ErrorModel
import models.frontend.FESuccessResponse
import org.mockito.Mockito._
import org.mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import services.SubscriptionStatusService
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.TestConstants._

import scala.concurrent.Future

trait TestSubscriptionStatusService extends MockOldBusinessDetailsConnector with MockBusinessDetailsConnector {

  val mockServicesConfig: ServicesConfig = mock[ServicesConfig]
  val mockConfiguration: Configuration = mock[Configuration]
  val mockConfig: MicroserviceAppConfig = new MicroserviceAppConfig(mockServicesConfig, mockConfiguration)

  object TestSubscriptionStatusService extends SubscriptionStatusService(
    mockConfig,
    mockOldBusinessDetailsConnector,
    mockBusinessDetailsConnector
  )

}

trait MockSubscriptionStatusService extends MockitoSugar {

  val mockSubscriptionStatusService: SubscriptionStatusService = mock[SubscriptionStatusService]

  private def mockCheckMtditsaSubscription(nino: String)(response: Future[Either[ErrorModel, Option[FESuccessResponse]]]): Unit =
    when(mockSubscriptionStatusService.checkMtditsaSubscription(ArgumentMatchers.eq(nino))(
      ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
    ).thenReturn(response)

  def mockCheckMtditsaFound(nino: String): Unit =
    mockCheckMtditsaSubscription(nino)(Future.successful(Right(Some(FESuccessResponse(Some(testMtditId))))))

  def mockCheckMtditsaNotFound(nino: String): Unit =
    mockCheckMtditsaSubscription(nino)(Future.successful(Right(Some(FESuccessResponse(None)))))

  def mockCheckMtditsaFailure(nino: String): Unit =
    mockCheckMtditsaSubscription(nino)(Future.successful(Left(INVALID_NINO_MODEL)))

}
