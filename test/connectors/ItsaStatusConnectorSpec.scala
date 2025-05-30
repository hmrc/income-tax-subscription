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

package connectors

import common.CommonSpec
import config.AppConfig
import connectors.mocks.MockHttp
import models.ErrorModel
import models.status.ITSAStatus.MTDVoluntary
import models.status.{ItsaStatusResponse, TaxYearStatus}
import models.subscription.AccountingPeriodUtil
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import parsers.ItsaStatusParser.GetItsaStatusResponse
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class ItsaStatusConnectorSpec extends CommonSpec with MockHttp with GuiceOneAppPerSuite {
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val request: Request[_] = FakeRequest()

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  val connector = new ItsaStatusConnector(mockHttpClient, appConfig)

  "getItsaStatus" should {
    "retrieve the user ITSA status" when {
      "the status-determination-service returns a successful response" in {
        val expectedResponse = ItsaStatusResponse(
          List(
            TaxYearStatus("2022-23", MTDVoluntary),
            TaxYearStatus("2023-24", MTDVoluntary)
          )
        )

        mockItsaStatusResponse(Future.successful(Right(expectedResponse)))

        await(
          connector.getItsaStatus("test-nino", "test-utr")
        ) shouldBe Right(expectedResponse)
      }
    }

    "return an error" when {
      "the status-determination-service returns a failed response" in {
        val expectedResponse = ErrorModel(INTERNAL_SERVER_ERROR, """{failures:[{"code":"code","reason":"reason"}]}""")

        mockItsaStatusResponse(Future.successful(Left(expectedResponse)))

        await(
          connector.getItsaStatus("test-nino", "test-utr")
        ) shouldBe Left(expectedResponse)
      }
    }
  }

  private def mockItsaStatusResponse(expectedResponse: Future[GetItsaStatusResponse]) = {
    val years: String = AccountingPeriodUtil.getCurrentTaxYear.toItsaStatusShortTaxYear
    val url = s"${appConfig.statusDeterminationServiceURL}/income-tax/itsa-status/test-nino/test-utr/$years"

    when(mockHttpClient.GET[GetItsaStatusResponse](
      ArgumentMatchers.eq(url),
      ArgumentMatchers.any(),
      ArgumentMatchers.any()
    )(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any[ExecutionContext])
    ).thenReturn(expectedResponse)
  }
}
