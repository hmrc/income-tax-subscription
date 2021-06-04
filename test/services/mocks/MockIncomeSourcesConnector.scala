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

package services.mocks

import config.AppConfig
import connectors.CreateIncomeSourcesConnector
import connectors.mocks.MockHttp
import models.subscription.BusinessSubscriptionDetailsModel
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, when}
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import parsers.CreateIncomeSourceParser.PostIncomeSourceResponse
import play.api.mvc.Request
import services.monitoring.AuditService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MockIncomeSourcesConnector extends MockitoSugar with BeforeAndAfterEach with MockHttp with GuiceOneAppPerSuite {
  this: Suite =>

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockIncomeSourcesConnector)
  }
  val mockIncomeSourcesConnector: CreateIncomeSourcesConnector = mock[CreateIncomeSourcesConnector]
  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  val auditService: AuditService = app.injector.instanceOf[AuditService]
  val connector = new CreateIncomeSourcesConnector(mockHttpClient, appConfig, auditService)


  def createBusinessIncome(agentreferenceNumber: Option[String], mtdbsaRef: String, incomeSourceRequest: BusinessSubscriptionDetailsModel)(response: Future[PostIncomeSourceResponse])(implicit hc: HeaderCarrier): Unit = {
    when(mockIncomeSourcesConnector.createBusinessIncome(ArgumentMatchers.eq(agentreferenceNumber),ArgumentMatchers.eq(mtdbsaRef),ArgumentMatchers.eq(incomeSourceRequest))
    (ArgumentMatchers.any[HeaderCarrier],ArgumentMatchers.any[Request[_]])).thenReturn(response)

  }
}
