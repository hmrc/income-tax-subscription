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

package connectors

import common.CommonSpec
import config.AppConfig
import connectors.mocks.MockHttp
import models.status.MandationStatus.Voluntary
import models.status.{MandationStatusRequest, MandationStatusResponse}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import parsers.MandationStatusParser.PostMandationStatusResponse
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MandationStatusConnectorSpec extends CommonSpec with MockHttp with GuiceOneAppPerSuite {
  implicit val hc = HeaderCarrier()
  implicit val request: Request[_] = FakeRequest()

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  val connector = new MandationStatusConnector(mockHttpClient, appConfig)

  val headers = Seq()

  "MandationStatusConnector" should {
    "retrieve the user mandation status" in {
      when(mockHttpClient.POST[MandationStatusRequest, PostMandationStatusResponse](
        ArgumentMatchers.eq(s"${appConfig.statusDeterminationServiceURL}/itsa-status"),
        ArgumentMatchers.eq(MandationStatusRequest("test-nino", "test-utr")),
        ArgumentMatchers.any()
      )(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Right(MandationStatusResponse(currentYearStatus = Voluntary, nextYearStatus = Voluntary))))

      await(
        connector.getMandationStatus("test-nino", "test-utr")
      ) shouldBe Right(MandationStatusResponse(currentYearStatus = Voluntary, nextYearStatus = Voluntary))
    }
  }
}
