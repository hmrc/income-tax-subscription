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

package controllers.throttle

import common.CommonSpec
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, when}
import play.api.http.Status.{BAD_REQUEST, OK, SERVICE_UNAVAILABLE}
import play.api.mvc.{AnyContent, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, status, stubControllerComponents}
import services.mocks.{MockAuthService, MockThrottlingRepository}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.MaterializerSupport

import scala.concurrent.Future

class ThrottlingControllerSpec extends CommonSpec
  with MockAuthService with MockThrottlingRepository with MaterializerSupport {

  val mockServicesConfig: ServicesConfig = mock[ServicesConfig]
  lazy val mockCC = stubControllerComponents()
  val request: Request[AnyContent] = FakeRequest()

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockServicesConfig)
  }

  object TestController extends ThrottlingController(mockThrottleRepository, mockServicesConfig, mockCC)

  {

    val throttleId = "testThrottle"
    val throttleMaxKey = s"throttle.$throttleId.max"
    "Valid fetch" should {
      "return a success" when {
        "there is a lower-then-max value in the database" in {
          val lowUsage = 50
          when(mockThrottleRepository.checkThrottle(ArgumentMatchers.eq(throttleId))).thenReturn(Future.successful(lowUsage))
          val highMax = 100
          when(mockServicesConfig.getInt(ArgumentMatchers.eq(throttleMaxKey))).thenReturn(highMax)
          val result: Future[Result] = TestController.throttled(throttleId)(request)
          status(result) shouldBe OK
        }
      }
      "return a service unavailable" when {
        "there is a higher-then-max value in the database" in {
          val highUsage = 100
          when(mockThrottleRepository.checkThrottle(ArgumentMatchers.eq(throttleId))).thenReturn(Future.successful(highUsage))
          val lowMax = 50
          when(mockServicesConfig.getInt(ArgumentMatchers.eq(throttleMaxKey))).thenReturn(lowMax)
          val result: Future[Result] = TestController.throttled(throttleId)(request)
          status(result) shouldBe SERVICE_UNAVAILABLE
        }
      }
    }
    "Invalid fetch" should {
      "return a bad request" when {
        "there is not a correct value in the config" in {
          when(mockServicesConfig.getInt(ArgumentMatchers.eq(throttleMaxKey))).thenThrow(new RuntimeException())
          val result: Future[Result] = TestController.throttled(throttleId)(request)
          status(result) shouldBe BAD_REQUEST
        }
      }
    }
  }
}
