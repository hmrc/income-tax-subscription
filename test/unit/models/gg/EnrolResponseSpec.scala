/*
 * Copyright 2017 HM Revenue & Customs
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

package unit.models.gg

import models.gg.{EnrolResponse, TypeValuePair}
import play.api.libs.json.JsValue
import uk.gov.hmrc.play.test.UnitSpec
import utils.JsonUtils._
import utils.TestConstants.GG.EnrolResponseExamples

class EnrolResponseSpec extends UnitSpec {

  import EnrolResponseExamples._

  "EnrolResponse" should {
    "Provide the correct reader for EnrolResponse" in {
      val response: JsValue = EnrolResponse(
        serviceName,
        state,
        friendlyName,
        List(
          TypeValuePair(testType1, testValue1),
          TypeValuePair(testType2, testValue2)
        )
      )

      val expected = jsonEnrolResponse(
        serviceName,
        state,
        friendlyName,
        List(
          TypeValuePair(testType1, testValue1),
          TypeValuePair(testType2, testValue2)
        )
      )

      response shouldBe expected
    }
  }

}