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

package services

import common.CommonSpec
import models.ErrorModel
import models.frontend.FESuccessResponse
import models.registration.GetBusinessDetailsSuccessResponseModel
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import parsers.GetITSABusinessDetailsParser.{AlreadySignedUp, NotSignedUp}
import play.api.http.Status._
import play.api.mvc.Request
import play.api.test.FakeRequest
import services.mocks.TestSubscriptionStatusService
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestConstants._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubscriptionStatusServiceSpec extends CommonSpec with TestSubscriptionStatusService {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val request: Request[_] = FakeRequest()

  "SubscriptionStatusService.checkMtditsaSubscription" when {
    "return some mtditsa id" when {
      "he new connector returns a successful response" in {
        when(mockITSABusinessDetailsConnector.getHIPBusinessDetails(ArgumentMatchers.eq(testNino))(
          ArgumentMatchers.any[HeaderCarrier], ArgumentMatchers.any[Request[_]]
        )).thenReturn(Future.successful(AlreadySignedUp(testMtditId)))

        NewTestSubscriptionStatusService
          .checkMtditsaSubscription(testNino)
          .futureValue shouldBe Right(Some(FESuccessResponse(Some(testMtditId))))
      }
    }
    "return no identifier" when {
      "the new connector indicates that no subscription was found" in {
        when(mockITSABusinessDetailsConnector.getHIPBusinessDetails(ArgumentMatchers.eq(testNino))(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(NotSignedUp))

        NewTestSubscriptionStatusService
          .checkMtditsaSubscription(testNino)
          .futureValue shouldBe Right(None)
      }
    }
  }
}
