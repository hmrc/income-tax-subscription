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

package controllers.subscription

import audit.Logging
import models.frontend.FESuccessResponse
import play.api.http.Status._
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.stubControllerComponents
import services.mocks.{MockAuthService, MockSubscriptionStatusService}
import uk.gov.hmrc.play.test.UnitSpec
import utils.MaterializerSupport
import utils.TestConstants._

import scala.concurrent.Future

class SubscriptionStatusControllerSpec extends UnitSpec with MockSubscriptionStatusService with MaterializerSupport with MockAuthService {
  lazy val mockCC = stubControllerComponents()
  val logging = mock[Logging]

  object TestController extends SubscriptionStatusController(logging, mockAuthService, mockSubscriptionStatusService, mockCC)

  def call: Future[Result] = TestController.checkSubscriptionStatus(testNino)(FakeRequest())

  "SubscriptionStatusController" should {
    "when the queried person has no prior mtditsa subscription return OK with an empty body" in {
      mockCheckMtditsaNotFound(testNino)
      mockAuthSuccess()

      val result = call
      status(result) shouldBe OK
      jsonBodyOf(result).as[FESuccessResponse].mtditId shouldBe None
    }

    "when the queried person has a prior mtditsa subscription return OK with the id" in {
      mockCheckMtditsaFound(testNino)
      mockAuthSuccess()

      val result = call
      status(result) shouldBe OK
      jsonBodyOf(result).as[FESuccessResponse].mtditId shouldBe Some(testMtditId)
    }
  }

}
