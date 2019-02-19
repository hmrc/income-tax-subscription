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

import play.api.http.Status._
import services.mocks.TestRegistrationService
import utils.TestConstants._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

class RegistrationServiceSpec extends UnitSpec with TestRegistrationService {

  implicit val hc = HeaderCarrier()

  def call = await(TestRegistrationService.register(isAgent = false, testNino))

  "RegistrationService" should {

    "return the safeId when the registration is successful" in {
      mockRegisterSuccess(testNino, registerRequestPayload)
      call.right.get.safeId shouldBe testSafeId
    }

    "return the error if registration fails" in {
      mockRegisterFailure(testNino, registerRequestPayload)
      call.left.get.status shouldBe BAD_REQUEST
    }

    "return the safeId when the registration is conflict but lookup is successful" in {
      mockRegisterConflict(testNino, registerRequestPayload)
      mockGetRegistrationSuccess(testNino)
      call.right.get.safeId shouldBe testSafeId
    }

    "return the error when both registration is conflict but lookup is unsuccessful" in {
      mockRegisterConflict(testNino, registerRequestPayload)
      mockGetRegistrationFailure(testNino)
      call.left.get.status shouldBe BAD_REQUEST
    }
  }

}
