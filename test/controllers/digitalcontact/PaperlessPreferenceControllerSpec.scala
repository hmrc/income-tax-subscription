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

package controllers.digitalcontact

import common.Constants._
import models.digitalcontact.PaperlessPreferenceKey
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.Helpers.stubControllerComponents
import play.api.test.FakeRequest
import services.mocks.{MockAuthService, MockPaperlessPreferenceService}
import uk.gov.hmrc.play.test.UnitSpec
import utils.MaterializerSupport
import utils.TestConstants._
import play.api.test.Helpers._
import play.api.test.Helpers.stubControllerComponents
import uk.gov.hmrc.play.bootstrap

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PaperlessPreferenceControllerSpec extends UnitSpec with MaterializerSupport with MockPaperlessPreferenceService with MockAuthService {

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

      intercept[Exception](await(res)) shouldBe testException
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

      val request: FakeRequest[JsValue] = FakeRequest().withBody(Json.obj())

      val res: Future[Result] = TestPaperlessPreferenceController.getNino(testPreferencesToken)(FakeRequest())

      intercept[Exception](await(res)) shouldBe testException
    }
  }

}
