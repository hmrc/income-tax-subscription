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

package controllers

import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{stubControllerComponents, _}
import services.mocks.{MockAuthService, MockSelfEmploymentsService}
import uk.gov.hmrc.play.test.UnitSpec
import utils.MaterializerSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SelfEmploymentsControllerSpec extends UnitSpec with MockSelfEmploymentsService with MockAuthService {

  object TestController extends SelfEmploymentsController(mockAuthService, mockSelfEmploymentsService, stubControllerComponents())

  val testJson: JsObject = Json.obj(
    "testDataIdKey" -> "testDataIdValue"
  )

  val request = FakeRequest()

  "getAllSelfEmployments" should {
    "return OK" when {
      "some data is returned from the service" in {
        mockAuthSuccess()
        mockGetAllSelfEmployments(Some(testJson))

        val result = TestController.getAllSelfEmployments(request)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe testJson
      }
    }

    "return NoContent" when {
      "no data is returned from the service" in {
        mockAuthSuccess()
        mockGetAllSelfEmployments(None)

        val result = TestController.getAllSelfEmployments(request)

        status(result) shouldBe NO_CONTENT
      }
    }
  }

  "retrieveSelfEmployments" should {
    "return OK" when {
      "some data related to the dataId is returned from the service" in {
        mockAuthSuccess()
        mockRetrieveSelfEmployments(Some(testJson))

        val result = TestController.retrieveSelfEmployments(mockDataId)(request)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe testJson
      }
    }

    "return NoContent" when {
      "no data is returned from the service for the dataId" in {
        mockAuthSuccess()
        mockRetrieveSelfEmployments(None)

        val result = TestController.retrieveSelfEmployments(mockDataId)(request)

        status(result) shouldBe NO_CONTENT
      }
    }
  }

  "insertSelfEmployments" should {
    "return OK" when {
      "the result is returned from the service" in {
        val fakeRequest = FakeRequest().withBody(testJson)

        mockAuthSuccess()
        mockInsertSelfEmployments(testJson)(Some(testJson))

        val result: Future[Result] = TestController.insertSelfEmployments(mockDataId)(fakeRequest)

        status(result) shouldBe OK
      }
    }
  }

}
