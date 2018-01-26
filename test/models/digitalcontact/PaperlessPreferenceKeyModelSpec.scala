/*
 * Copyright 2018 HM Revenue & Customs
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

package models.digitalcontact

import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec
import utils.Implicits
import utils.TestConstants._

class PaperlessPreferenceKeyModelSpec extends UnitSpec with Implicits {

  "model writer" should {
    import utils.TestConstants.PaperlessPreferenceResponse.successResponse

    "format the class into the expected format" in {
      val token = "ABC"
      val nino = testNino
      val response: PaperlessPreferenceKey = PaperlessPreferenceKey(token, nino)
      Json.toJson[PaperlessPreferenceKey](response) shouldBe successResponse(nino)

    }
  }

}
