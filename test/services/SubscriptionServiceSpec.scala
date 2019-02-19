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

package services

import models.ErrorModel
import models.subscription.IncomeSourceModel
import models.subscription.property.PropertySubscriptionResponseModel
import services.mocks.TestSubscriptionService
import utils.TestConstants._

import scala.util.Right
import uk.gov.hmrc.http.HeaderCarrier

class SubscriptionServiceSpec extends TestSubscriptionService {

  implicit val hc = HeaderCarrier()

  "SubscriptionService" should {

    def propertySubscribeCall: Either[ErrorModel, PropertySubscriptionResponseModel] = await(TestSubscriptionService.propertySubscribe(fePropertyRequest))

    "return success if the subscription succeeds" in {
      mockPropertySubscribe(testNino)(Right(propertySubscriptionSuccess))
      propertySubscribeCall shouldBe Right(propertySubscriptionSuccess)
    }

    "return not found response" in {

      mockPropertySubscribe(testNino)(Left(NOT_FOUND_NINO_MODEL))
      propertySubscribeCall shouldBe Left(NOT_FOUND_NINO_MODEL)
    }
  }

}
