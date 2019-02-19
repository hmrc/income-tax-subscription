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
import connectors.mocks.MockSubscriptionConnector
import models.ErrorModel
import models.frontend.FERequest
import models.subscription.business.BusinessSubscriptionSuccessResponseModel
import models.subscription.property.PropertySubscriptionResponseModel
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import services.SubscriptionService
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

trait MockSubscriptionService extends MockitoSugar {
  val mockSubscriptionService = mock[SubscriptionService]

  def mockBusinessSubscribe(request: FERequest)(response: Future[Either[ErrorModel, BusinessSubscriptionSuccessResponseModel]]): Unit = {
    when(mockSubscriptionService.businessSubscribe(
      ArgumentMatchers.eq(request)
    )(
      ArgumentMatchers.any[HeaderCarrier]
    ))
      .thenReturn(response)
  }

  def mockPropertySubscribe(request: FERequest)(response: Future[Either[ErrorModel, PropertySubscriptionResponseModel]]): Unit = {
    when(mockSubscriptionService.propertySubscribe(
      ArgumentMatchers.eq(request)
    )(
      ArgumentMatchers.any[HeaderCarrier]
    ))
      .thenReturn(response)
  }
}

trait TestSubscriptionService extends MockSubscriptionConnector with UnitSpec with GuiceOneAppPerSuite {
  lazy val logging: Logging = app.injector.instanceOf[Logging]

  object TestSubscriptionService extends SubscriptionService(mockSubscriptionConnector, logging)
}
