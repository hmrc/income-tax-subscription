/*
 * Copyright 2023 HM Revenue & Customs
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

import common.CommonSpec
import config.AppConfig
import connectors.mocks.MockHttp
import models.ErrorModel
import models.hip.{SelfEmp, SelfEmpHolder}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import parsers.hip.HipPrePopParser.{GetHipPrePopResponse, GetHipPrePopResponseHttpReads}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, SERVICE_UNAVAILABLE}
import play.api.libs.json.Json
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HipPrePopConnectorSpec extends CommonSpec with MockHttp with GuiceOneAppPerSuite {

  "getHipPrePopData" should {
    "retrieve prepop data" when {
      "the HIP API #1933 returns a successful response" in {
        val data: SelfEmpHolder = SelfEmpHolder(
          selfEmp = Some(Seq(SelfEmp(
            businessName = Some("ABC Plumbers"),
            businessDescription = Some("Plumber"),
            businessAddressFirstLine = Some("1 Hazel Court"),
            businessAddressPostcode = Some("AB12 3CD"),
            dateBusinessStarted = Some("2011-08-14")
          )))
        )

        val response = HttpResponse(
          OK,
          Json.toJson(data).toString
        )

        GetHipPrePopResponseHttpReads.read("", "", response) shouldBe
          Right(data)
      }

      "the HIP API #1933 returns invalid Json" in {
        val response = HttpResponse(
          OK,
          Json.obj("selfEmp" -> "").toString
        )

        GetHipPrePopResponseHttpReads.read("", "", response) shouldBe
          Left(ErrorModel(OK, "Failure parsing json response from prepop api"))
      }

      "the HIP API #1933 returns an unexpected status" in {
        val response = HttpResponse(
          INTERNAL_SERVER_ERROR,
          Json.obj().toString
        )

        GetHipPrePopResponseHttpReads.read("", "", response) shouldBe
          Left(ErrorModel(INTERNAL_SERVER_ERROR, "Unexpected status returned from pre-pop api"))
      }
    }
  }
}
