/*
 * Copyright 2016 HM Revenue & Customs
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

package routesSpec

import org.scalatest._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class SandboxRoutesSpec extends UnitSpec with WithFakeApplication with Matchers {
  "The URL for the subscribe Action" should {
    "be equal to /sandbox/subscription" in {
      val path = controllers.sandbox.routes.SandboxSubscriptionController.subscribe().url
      path shouldEqual "/sandbox/subscription"
    }
  }
}
