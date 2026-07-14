/*
 * Copyright 2020 HM Revenue & Customs
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

package connectors.hip

import helpers.WiremockHelper.StubResponse
import helpers.{ComponentSpecBase, WiremockHelper}
import models.hip.{SelfEmp, SelfEmpHolder}
import play.api.http.Status.{BAD_GATEWAY, INTERNAL_SERVER_ERROR, OK, SERVICE_UNAVAILABLE, TOO_MANY_REQUESTS, BAD_REQUEST, NOT_FOUND}
import play.api.libs.json.Json
import play.api.test.Helpers.{await, defaultAwaitTimeout}

class HipPrePopConnectorISpec extends ComponentSpecBase {

  private lazy val connector: HipPrePopConnector = app.injector.instanceOf[HipPrePopConnector]

  val testNino: String = "test-nino"
  val url: String = s"/cesa/prepopulation/businessdata/$testNino"

  val testSelfEmpHolder: SelfEmpHolder = SelfEmpHolder(
    selfEmp = Some(Seq(SelfEmp(
      businessName = Some("ABC Plumbers"),
      businessDescription = Some("Plumber"),
      businessAddressFirstLine = Some("1 Hazel Court"),
      businessAddressPostcode = Some("AB12 3CD"),
      dateBusinessStarted = Some("2011-08-14")
    )))
  )

  "getHipPrePopData" when {

    "retry" should {
      Seq(BAD_GATEWAY, TOO_MANY_REQUESTS, SERVICE_UNAVAILABLE, INTERNAL_SERVER_ERROR).foreach { status =>
        s"For a return status of $status" in {
          val nino = s"test-nino-$status"
          val statusUrl = s"/cesa/prepopulation/businessdata/$nino"

          WiremockHelper.stubGetSequence(statusUrl)(
            StubResponse(status),
            StubResponse(status),
            StubResponse(OK, Json.toJson(testSelfEmpHolder))
          )

          val result = await(connector.getHipPrePopData(nino))

          result shouldBe Right(testSelfEmpHolder)

          WiremockHelper.verifyGet(uri = statusUrl, times = 3)
        }
      }
    }

    "retries are exhausted" should {
      "return a Left after repeated failures" in {
        val nino = "test-nino-exhausted"
        val exhaustedUrl = s"/cesa/prepopulation/businessdata/$nino"

        WiremockHelper.stubGetSequence(exhaustedUrl)(
          StubResponse(INTERNAL_SERVER_ERROR),
          StubResponse(INTERNAL_SERVER_ERROR),
          StubResponse(INTERNAL_SERVER_ERROR),
          StubResponse(INTERNAL_SERVER_ERROR)
        )

        val result = await(connector.getHipPrePopData(nino))

        result.isLeft shouldBe true

        WiremockHelper.verifyGet(uri = exhaustedUrl, times = 4)
      }
    }
  }

  "an unrecognised status is returned" should {
    "not retry and return a Left immediately" in {
      val nino = "test-nino-no-retry"
      val noRetryUrl = s"/cesa/prepopulation/businessdata/$nino"

      WiremockHelper.stubGet(noRetryUrl, BAD_REQUEST, Json.stringify(Json.obj()))

      val result = await(connector.getHipPrePopData(nino))

      result.isLeft shouldBe true

      WiremockHelper.verifyGet(uri = noRetryUrl, times = 1)
    }
  }

  "a NOT_FOUND response is returned" should {
    "return an empty result without retrying" in {
      val nino = "test-nino-not-found"
      val notFoundUrl = s"/cesa/prepopulation/businessdata/$nino"

      WiremockHelper.stubGet(notFoundUrl, NOT_FOUND, Json.stringify(Json.obj()))

      val result = await(connector.getHipPrePopData(nino))

      result shouldBe Right(SelfEmpHolder(None))

      WiremockHelper.verifyGet(uri = notFoundUrl, times = 1)
    }
  }
}