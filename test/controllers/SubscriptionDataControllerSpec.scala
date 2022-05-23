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

package controllers

import common.CommonSpec
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import reactivemongo.api.commands.UpdateWriteResult
import services.SubscriptionDataService.{Created, Existing}
import services.mocks.{MockAuthService, MockSubscriptionDataService}
import uk.gov.hmrc.auth.core.retrieve.Credentials

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubscriptionDataControllerSpec extends CommonSpec with MockSubscriptionDataService with MockAuthService {

  object TestController extends SubscriptionDataController(mockAuthService, mockSubscriptionDataService, stubControllerComponents())

  val testJson: JsObject = Json.obj(
    "testDataIdKey" -> "testDataIdValue"
  )
  val reference: String = "test-reference"

  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  "retrieveReference" should {
    "return an InternalServerError" when {
      "the user has no credentials" in {
        mockRetrievalSuccess[Option[Credentials]](None)

        val result = TestController.retrieveReference()(request.withBody(Json.obj()))

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
      "an invalid payload is received" in {
        mockRetrievalSuccess[Option[Credentials]](Some(Credentials("test-cred-id", "ggProvider")))

        val result = TestController.retrieveReference()(request.withBody(Json.obj()))

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
    "return Ok with a reference" when {
      "SubscriptionDataService returns an Existing reference" in {
      mockRetrievalSuccess[Option[Credentials]](Some(Credentials("test-cred-id", "ggProvider")))
      mockRetrieveReference("1234567890", Existing, "test-cred-id")(reference)

      val result = TestController.retrieveReference()(request.withBody(Json.obj("utr" -> "1234567890")))

      status(result) shouldBe OK
    }
  }
    "return Created with a reference" when {
      "SubscriptionDataService returns an Created reference" in {
        mockRetrievalSuccess[Option[Credentials]](Some(Credentials("test-cred-id", "ggProvider")))
        mockRetrieveReference("1234567890", Created, "test-cred-id")(reference)

        val result = TestController.retrieveReference()(request.withBody(Json.obj("utr" -> "1234567890")))

        status(result) shouldBe CREATED
      }
    }
  }

  "getAllSelfEmployments" should {
    "return OK" when {
      "some data is returned from the service" in {
        mockAuthSuccess()

        mockGetAllSelfEmployments(reference)(Some(testJson))

        val result = TestController.getAllSubscriptionData(reference)(request)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe testJson
      }
    }

    "return NoContent" when {
      "no data is returned from the service" in {
        mockAuthSuccess()
        mockGetAllSelfEmployments(reference)(None)

        val result = TestController.getAllSubscriptionData(reference)(request)

        status(result) shouldBe NO_CONTENT
      }
    }
  }

  "retrieveSelfEmployments" should {
    "return OK" when {
      "some data related to the dataId is returned from the service" in {
        mockAuthSuccess()
        mockRetrieveSelfEmployments(reference)(Some(testJson))

        val result = TestController.retrieveSubscriptionData(reference, mockDataId)(request)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe testJson
      }
    }

    "return NoContent" when {
      "no data is returned from the service for the dataId" in {
        mockAuthSuccess()
        mockRetrieveSelfEmployments(reference)(None)

        val result = TestController.retrieveSubscriptionData(reference, mockDataId)(request)

        status(result) shouldBe NO_CONTENT
      }
    }
  }

  "insertSelfEmployments" should {
    "return OK" when {
      "the result is returned from the service" in {
        val fakeRequest = FakeRequest().withBody(testJson)

        mockAuthSuccess()
        mockInsertSelfEmployments(reference, testJson)(Some(testJson))

        val result: Future[Result] = TestController.insertSubscriptionData(reference, mockDataId)(fakeRequest)

        status(result) shouldBe OK
      }
    }
  }

  "deleteSelfEmployments" should {
    "return OK" when {
      "the result is returned from the service" in {
        mockAuthSuccess()
        mockDeleteSubscriptionData(reference, testJson)(Some(testJson))

        val result: Future[Result] = TestController.deleteSubscriptionData(reference, mockDataId)(request)

        status(result) shouldBe OK
      }
    }
  }

  "deleteAllSessionData" should {
    "return OK" when {
      "the data related to the given sessionId have been deleted successfully and an OK status returned from the service" in {
        mockAuthSuccess()
        mockDeleteSessionData(reference)(UpdateWriteResult(ok = true, 1, 1, Seq(), Seq(), None, None, None))

        val result = TestController.deleteAllSubscriptionData(reference)(request)

        status(result) shouldBe OK
      }
    }

    "throw an exception" when {
      "delete session data fails" in {
        mockAuthSuccess()
        mockDeleteSessionData(reference)(UpdateWriteResult(ok = false, 1, 1, Seq(), Seq(), None, Some(INTERNAL_SERVER_ERROR), None))

        val ex = intercept[RuntimeException](await(TestController.deleteAllSubscriptionData(reference)(request)))
        ex.getMessage shouldBe "[SubscriptionDataController][deleteAllSessionData] - delete session data failed with code 500"
      }
    }
  }

}
