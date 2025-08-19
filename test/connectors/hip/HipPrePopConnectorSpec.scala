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
import parsers.hip.HipPrePopParser.GetHipPrePopResponse
import play.api.http.Status.SERVICE_UNAVAILABLE
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HipPrePopConnectorSpec extends CommonSpec with MockHttp with GuiceOneAppPerSuite {
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val request: Request[_] = FakeRequest()

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  val connector = new HipPrePopConnector(mockHttpClient, appConfig)

  "getHipPrePopData" should {
    "retrieve prepop data" when {
      "the HIP API #1933 returns a successful response" in {
        val data: SelfEmpHolder = SelfEmpHolder(
          selfEmp = Seq(SelfEmp(
            businessName = Some("ABC Plumbers"),
            businessDescription = Some("Plumber"),
            businessAddressFirstLine = Some("1 Hazel Court"),
            businessAddressPostcode = Some("AB12 3CD"),
            dateBusinessStarted = Some("2011-08-14")
          ))
        )

        when(mockHttpClient.GET[GetHipPrePopResponse](any(), any(), any())(any(), any(), any())).thenReturn(
          Future.successful(Right(data))
        )

        await(
          connector.getHipPrePopData("test-nino")
        ) shouldBe Right(data)
      }

      "the HIP API #1933 returns an error" in {
        val error = ErrorModel(SERVICE_UNAVAILABLE, "")

        when(mockHttpClient.GET[GetHipPrePopResponse](any(), any(), any())(any(), any(), any())).thenReturn(
          Future.successful(Left(error))
        )

        await(
          connector.getHipPrePopData("test-nino")
        ) shouldBe Left(error)
      }
    }
  }
}
