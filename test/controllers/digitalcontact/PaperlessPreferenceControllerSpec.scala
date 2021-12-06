/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.digitalcontact

import common.CommonSpec
import common.Constants._
import models.digitalcontact.PaperlessPreferenceKey
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.mocks.{MockAuthService, MockPaperlessPreferenceService}
import utils.MaterializerSupport
import utils.TestConstants._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PaperlessPreferenceControllerSpec extends CommonSpec with MaterializerSupport with MockPaperlessPreferenceService with MockAuthService {

  lazy val mockCC = stubControllerComponents()

  object TestPaperlessPreferenceController extends PaperlessPreferenceController(
    mockAuthService,
    mockPaperlessPreferenceService,
    mockCC
  )

  s"storeNino($testPreferencesToken)" should {
    "return OK when the NINO is successfully stored against the token" in {
      mockAuthSuccess()
      mockNinoStore(testPaperlessPreferenceKey)

      val request: FakeRequest[JsValue] = FakeRequest().withBody(Json.obj(ninoJsonKey -> testNino))

      val res: Future[Result] = TestPaperlessPreferenceController.storeNino(testPreferencesToken)(request)

      status(res) shouldBe CREATED
    }

    "fail if the storage fails" in {
      mockAuthSuccess()
      mockNinoStoreFailed(testPaperlessPreferenceKey)

      val request: FakeRequest[JsValue] = FakeRequest().withBody(Json.obj(ninoJsonKey -> testNino))

      val res: Future[Result] = TestPaperlessPreferenceController.storeNino(testPreferencesToken)(request)

      res.failed.futureValue shouldBe testException
    }

    "return a bad request when the json cannot be parsed" in {
      mockAuthSuccess()
      mockNinoStoreFailed(testPaperlessPreferenceKey)

      val request: FakeRequest[JsValue] = FakeRequest().withBody(Json.obj())

      val res: Future[Result] = TestPaperlessPreferenceController.storeNino(testPreferencesToken)(request)

      status(res) shouldBe BAD_REQUEST
    }
  }

  s"getNino($testPreferencesToken)" should {
    "return successful NINO found" in {
      mockAuthSuccess()
      mockNinoGetFound(testPreferencesToken)

      val res: Future[Result] = TestPaperlessPreferenceController.getNino(testPreferencesToken)(FakeRequest())

      status(res) shouldBe OK
      val content = Json.parse(contentAsString(res))
      content shouldBe Json.toJson(testPaperlessPreferenceKey)(PaperlessPreferenceKey.writes)

    }

    "return successful NINO not found" in {
      mockAuthSuccess()
      mockNinoGetNotFound(testPreferencesToken)

      val res: Future[Result] = TestPaperlessPreferenceController.getNino(testPreferencesToken)(FakeRequest())

      status(res) shouldBe NOT_FOUND
    }

    "return an exception" in {
      mockAuthSuccess()
      mockNinoGetFailed(testPreferencesToken)

      val res: Future[Result] = TestPaperlessPreferenceController.getNino(testPreferencesToken)(FakeRequest())

      res.failed.futureValue shouldBe testException
    }
  }

}
