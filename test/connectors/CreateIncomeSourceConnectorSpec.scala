/*
 * Copyright 2021 HM Revenue & Customs
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

import config.AppConfig
import connectors.mocks.MockHttp
import models.monitoring.SignUpCompleteAudit
import models.subscription._
import models.subscription.business.{Cash, CreateIncomeSourceErrorModel, CreateIncomeSourceSuccessModel}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import parsers.CreateIncomeSourceParser.PostIncomeSourceResponse
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import services.mocks.monitoring.MockAuditService
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient}

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateIncomeSourceConnectorSpec extends MockHttp with GuiceOneAppPerSuite with MockAuditService {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val request: Request[_] = FakeRequest()

  class Test(mtdbsaRef: String, expectedBody: JsValue, response: PostIncomeSourceResponse) {
    val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
    val httpClient: HttpClient = mockHttpClient
    val headers = Seq(
      HeaderNames.authorisation -> appConfig.desAuthorisationToken,
      appConfig.desEnvironmentHeader
    )

    when(httpClient.POST[JsValue, PostIncomeSourceResponse](
      ArgumentMatchers.eq(s"${appConfig.desURL}/income-tax/income-sources/mtdbsa/$mtdbsaRef/ITSA/business"),
      ArgumentMatchers.eq(expectedBody),
      ArgumentMatchers.eq(headers))(
      ArgumentMatchers.any(),
      ArgumentMatchers.any(),
      ArgumentMatchers.any(),
      ArgumentMatchers.any())).thenReturn(Future.successful(response))

    val connector = new CreateIncomeSourcesConnector(httpClient, appConfig, mockAuditService)
  }

  val testCreateIncomeSubmissionModel: BusinessSubscriptionDetailsModel = BusinessSubscriptionDetailsModel(
    nino = "AA111111A",
    accountingPeriod = AccountingPeriodModel(LocalDate.now(), LocalDate.now()),
    selfEmploymentsData = None,
    accountingMethod = None,
    incomeSource = FeIncomeSourceModel(selfEmployment = false, ukProperty = true, foreignProperty = false),
    propertyStartDate = Some(PropertyStartDateModel(LocalDate.now())),
    propertyAccountingMethod = Some(AccountingMethodPropertyModel(Cash))
  )

  "CreateIncomeSourcesConnector.createBusinessIncome" when {

    "receiving a 200 status" should {

      "return a success response" in new Test("XAIT0000006", Json.toJson(testCreateIncomeSubmissionModel), Right(CreateIncomeSourceSuccessModel())) {
        await(connector.createBusinessIncome(Some("1223456"), "XAIT0000006", testCreateIncomeSubmissionModel)) shouldBe Right(CreateIncomeSourceSuccessModel())
        verifyExtendedAudit(SignUpCompleteAudit(Some("1223456"),testCreateIncomeSubmissionModel, appConfig.desAuthorisationToken))
      }
    }

    "receiving a non-200 status" should {

      "return the error response" in new Test("XAIT0000006",
        Json.toJson(testCreateIncomeSubmissionModel), Left(CreateIncomeSourceErrorModel(INTERNAL_SERVER_ERROR, "error body"))) {
        await(connector.createBusinessIncome(Some("1223456"),"XAIT0000006", testCreateIncomeSubmissionModel)) shouldBe Left(
          CreateIncomeSourceErrorModel(INTERNAL_SERVER_ERROR, "error body"))
        verifyExtendedAudit(SignUpCompleteAudit(Some("1223456"),testCreateIncomeSubmissionModel, appConfig.desAuthorisationToken))
      }
    }
  }
}
