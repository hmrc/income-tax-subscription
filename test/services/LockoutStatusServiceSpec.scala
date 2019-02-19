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

package services

import services.mocks.TestLockoutStatusService
import uk.gov.hmrc.play.test.UnitSpec
import utils.TestConstants._

import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.hmrc.http.HeaderCarrier

class LockoutStatusServiceSpec extends UnitSpec with TestLockoutStatusService {

  implicit val hc = HeaderCarrier()

  "LockoutStatusService.lockoutAgent" should {
    def call = await(TestLockoutStatusService.lockoutAgent(testArn, testLockoutRequest))

    "return a testLockoutSuccess if the lock is created" in {
      mockLockCreated(testArn)
      val result = await(call)
      result shouldBe testLockoutSuccess
    }

    "return a testLockoutFailure if it fails" in {
      mockLockCreationFailed(testArn)
      val ex = intercept[Exception](await(call))
      ex shouldBe testException
    }
  }

  "LockoutStatusService.checkLockoutStatus" should {
    def call = await(TestLockoutStatusService.checkLockoutStatus(testArn))

    "return a testLockoutSuccess if they are locked out" in {
      mockLockedOut(testArn)
      val result = await(call)
      result shouldBe testLockoutSuccess
    }

    "return a testLockoutNone if they are not locked out" in {
      mockNotLockedOut(testArn)
      val result = await(call)
      result shouldBe testLockoutNone
    }

    "return a failed future if it fails" in {
      mockLockedOutFailure(testArn)
      val ex = intercept[Exception](await(call))
      ex shouldBe testException
    }
  }

}
