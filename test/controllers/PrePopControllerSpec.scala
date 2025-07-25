/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers

import common.CommonSpec
import config.MicroserviceAppConfig
import connectors.PrePopConnector
import connectors.hip.HipPrePopConnector
import models.PrePopData
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.mocks.MockAuthService
import services.monitoring.AuditService
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success
import utils.TestConstants.{hmrcAsAgent, testNino}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PrePopControllerSpec extends CommonSpec with MockAuthService {

  private lazy implicit val hc = HeaderCarrier()

  private lazy val mockCC = stubControllerComponents()

  private val mockAppConfig = mock[MicroserviceAppConfig]
  private val mockAuditService = mock[AuditService]
  private val mockPrePopConnector = mock[PrePopConnector]
  private val mockHipPrePopConnector = mock[HipPrePopConnector]

  val controller = new PrePopController(
    mockAuthService,
    mockAuditService,
    mockPrePopConnector,
    mockHipPrePopConnector,
    mockAppConfig,
    mockCC
  )

  private def setup(useHip: Boolean) = {
    val data = PrePopData(
      selfEmployment = None,
      ukPropertyAccountingMethod = None,
      foreignPropertyAccountingMethod = None
    )
    val hipData = Seq.empty
    reset(mockPrePopConnector)
    reset(mockHipPrePopConnector)
    mockRetrievalSuccess[Enrolments](Enrolments(Set(Enrolment(hmrcAsAgent, Seq(EnrolmentIdentifier("AgentReferenceNumber", testArn)), "Activated"))))
    when(mockAuditService.extendedAudit(any())(any(), any(), any())).thenReturn(
      Future.successful(Success)
    )
    when(mockPrePopConnector.getPrePopData(any())(any())).thenReturn(
      Future.successful(Right(data))
    )
    when(mockHipPrePopConnector.getHipPrePopData(any())(any())).thenReturn(
      Future.successful(Right(hipData))
    )
    when(mockAppConfig.useHipForPrePop).thenReturn(
      useHip
    )
  }

  "prePop" should {
    "not use HIP if feature switch is false" in {
      setup(false)
      await(controller.prePop(testNino)(FakeRequest()))
      verify(mockPrePopConnector, times(1)).getPrePopData(any())(any())
      verify(mockHipPrePopConnector, times(0)).getHipPrePopData(any())(any())
    }

    "use HIP if feature switch is true" in {
      setup(true)
      await(controller.prePop(testNino)(FakeRequest()))
      verify(mockPrePopConnector, times(0)).getPrePopData(any())(any())
      verify(mockHipPrePopConnector, times(1)).getHipPrePopData(any())(any())
    }
  }
}
