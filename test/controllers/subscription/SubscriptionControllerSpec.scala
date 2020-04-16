/*
 * Copyright 2020 HM Revenue & Customs
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

import models.ErrorModel
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.stubControllerComponents
import services.SubmissionOrchestrationService.SuccessfulSubmission
import services.mocks.{MockAuthService, MockSubmissionOrchestrationService}
import uk.gov.hmrc.play.test.UnitSpec
import utils.MaterializerSupport
import utils.TestConstants._

import scala.concurrent.{ExecutionContext, Future}

class SubscriptionControllerSpec(implicit val ec: ExecutionContext
                                ) extends UnitSpec with MockSubmissionOrchestrationService with MockAuthService with MaterializerSupport {
  lazy val mockCC = stubControllerComponents()

  object TestController extends SubscriptionController(mockSubmissionOrchestrationService, mockAuthService, mockCC)

  "IncomeSourceController" when {
    "subscribe" should {
      "return a 200 response with an id if the json is valid" in {
        val fakeRequest = FakeRequest().withBody(Json.toJson(testBothIncomeSourceModel))

        mockAuthSuccess()
        mockSubmit(testBothIncomeSourceModel)(Future.successful(Right(SuccessfulSubmission(testMtditId))))

        val result = await(TestController.subscribe(testNino)(fakeRequest))
        status(result) shouldBe OK
        jsonBodyOf(result).as[SuccessfulSubmission].mtditId shouldBe testMtditId
      }
      "return the status in the error model if one is returned from the service" in {
        val fakeRequest = FakeRequest().withBody(Json.toJson(testBothIncomeSourceModel))
        val errorModel = ErrorModel(INTERNAL_SERVER_ERROR, "test-error")

        mockAuthSuccess()
        mockSubmit(testBothIncomeSourceModel)(Future.successful(Left(errorModel)))

        val result = await(TestController.subscribe(testNino)(fakeRequest))
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
      "return a 400 response if the json can't be parsed" in {
        val fakeRequest: FakeRequest[JsValue] = FakeRequest().withBody(Json.obj())

        mockAuthSuccess()

        val result = await(TestController.subscribe(testNino)(fakeRequest))
        status(result) shouldBe BAD_REQUEST
      }
    }
  }
}
