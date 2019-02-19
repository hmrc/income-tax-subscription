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
import controllers.ITSASessionKeys
import models.frontend.FESuccessResponse
import play.api.http.Status._
import play.api.mvc.{AnyContentAsJson, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.stubControllerComponents
import services.mocks.{MockAuthService, MockSubscriptionManagerService}
import uk.gov.hmrc.play.test.UnitSpec
import utils.JsonUtils._
import utils.MaterializerSupport
import utils.TestConstants._

import scala.concurrent.Future

class SubscriptionControllerSpec extends UnitSpec with MockSubscriptionManagerService with MaterializerSupport with MockAuthService {
  lazy val mockCC = stubControllerComponents()
  val logging = mock[Logging]

  object TestController extends SubscriptionController(logging, mockSubscriptionManagerService, mockAuthService, mockCC)

  def call(request: Request[AnyContentAsJson]): Future[Result] = TestController.subscribe(testNino)(request)

  "SubscriptionController" should {

    "return the id when successful" in {
      val fakeRequest: FakeRequest[AnyContentAsJson] =
        FakeRequest()
          .withJsonBody(fePropertyRequest)
          .withHeaders(ITSASessionKeys.RequestURI -> "")
      mockAuthSuccess()
      mockRosmAndEnrol(fePropertyRequest, "")(Future.successful(Right(feSuccessResponse)))
      val result = call(fakeRequest)
      jsonBodyOf(result).as[FESuccessResponse].mtditId.get shouldBe testMtditId

    }

    "return failure when the json body cannot be parsed" in {
      val fakeRequest: FakeRequest[AnyContentAsJson] =
        FakeRequest()
          .withJsonBody("{}")
          .withHeaders(ITSASessionKeys.RequestURI -> "")

      mockAuthSuccess()

      val result = call(fakeRequest)
      status(result) shouldBe BAD_REQUEST
    }
  }

}
