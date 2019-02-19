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

import models.digitalcontact.PaperlessPreferenceKey
import services.mocks.TestPaperlessPreferenceService
import uk.gov.hmrc.play.test.UnitSpec
import utils.TestConstants._

class PaperlessPreferenceServiceSpec extends UnitSpec with TestPaperlessPreferenceService {
  "storeNino" should {
    "return the model when it is successfully stored" in {
      mockNinoStore(testPaperlessPreferenceKey)

      val res = TestPaperlessPreferenceService.storeNino(testPaperlessPreferenceKey)
      await(res) shouldBe testPaperlessPreferenceKey
    }

    "return the failure when the storage fails" in {
      mockNinoStoreFailed(testPaperlessPreferenceKey)

      val res = TestPaperlessPreferenceService.storeNino(testPaperlessPreferenceKey)
      intercept[Exception](await(res)) shouldBe testException
    }
  }

  "getNino" should {
    "successful response found" in {
      mockNinoRetrieve(testPreferencesToken)

      val res = TestPaperlessPreferenceService.getNino(testPreferencesToken)
      await(res) shouldBe Some(PaperlessPreferenceKey(testPreferencesToken, testNino))
    }
    "not found response" in {
      mockNinoRetrieveNotFound(testPreferencesToken)

      val res = TestPaperlessPreferenceService.getNino(testPreferencesToken)
      await(res) shouldBe None
    }

    "return exception when failed" in {
      mockNinoRetrieveFailed(testPreferencesToken)

      val res = TestPaperlessPreferenceService.getNino(testPreferencesToken)
      intercept[Exception](await(res)) shouldBe testException
    }

  }
}
