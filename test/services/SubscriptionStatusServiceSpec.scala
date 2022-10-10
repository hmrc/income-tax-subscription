/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.http.Status._
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import services.mocks.TestSubscriptionStatusService
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestConstants._

import scala.concurrent.ExecutionContext.Implicits.global

class SubscriptionStatusServiceSpec extends CommonSpec with TestSubscriptionStatusService {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val request: Request[_] = FakeRequest()

  "SubscriptionStatusService.checkMtditsaSubscroption" should {

    def call: Either[ErrorModel, Option[FESuccessResponse]] = {
      await(TestSubscriptionStatusService.checkMtditsaSubscription(testNino))
    }

    "return the Right(NONE) when the person does not have a mtditsa subscription" in {
      mockGetBusinessDetailsNotFound(testNino)

      call.toOption.get shouldBe None
    }

    "return the Right(Some(FESuccessResponse)) when the person already have a mtditsa subscription" in {
      mockGetBusinessDetailsSuccess(testNino)
      // testMtditId must be the same value defined in getBusinessDetailsSuccess
      call.toOption.get shouldBe Some(FESuccessResponse(Some(testMtditId)))
    }

    "return the error for other error type" in {
      mockGetBusinessDetailsFailure(testNino)
      call.swap.toOption.get.status shouldBe INTERNAL_SERVER_ERROR
    }

  }

}
