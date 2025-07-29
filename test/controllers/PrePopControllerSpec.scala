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
import config.{AppConfig, MicroserviceAppConfig}
import config.featureswitch.{FeatureSwitching, UseHIPForPrePop}
import connectors.PrePopConnector
import connectors.hip.HipPrePopConnector
import models.PrePopData
import models.hip.{SelfEmp, SelfEmpHolder}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.libs.json.Json
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

class PrePopControllerSpec extends CommonSpec with MockAuthService with FeatureSwitching {

  private lazy implicit val hc = HeaderCarrier()

  private lazy val mockCC = stubControllerComponents()

  override val appConfig = mock[MicroserviceAppConfig]

  private val mockAuditService = mock[AuditService]
  private val mockPrePopConnector = mock[PrePopConnector]
  private val mockHipPrePopConnector = mock[HipPrePopConnector]

  val controller = new PrePopController(
    mockAuthService,
    mockAuditService,
    mockPrePopConnector,
    mockHipPrePopConnector,
    appConfig,
    mockCC
  )

  val selfEmp = Seq(SelfEmpHolder(
    selfEmp = SelfEmp(
      businessName = Some("ABC Plumbers"),
      businessDescription = Some("Plumber"),
      businessAddressFirstLine = Some("1 Hazel Court"),
      businessAddressPostcode = Some("AB12 3CD"),
      dateBusinessStarted = Some("2011-08-14")
    )
  ))

  val data = PrePopData(
    selfEmployment = Some(selfEmp.map(_.toPrePopSelfEmployment())),
    ukPropertyAccountingMethod = None,
    foreignPropertyAccountingMethod = None
  )

  private def setup(useHip: Boolean) = {
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
      Future.successful(Right(selfEmp))
    )
    setState(UseHIPForPrePop, useHip)
  }

  "prePop" should {
    "not use HIP if feature switch is false" in {
      setup(false)
      val result = controller.prePop(testNino)(FakeRequest())
      status(result) mustBe OK
      contentAsJson(result) mustBe Json.toJson(data)
      verify(mockPrePopConnector, times(1)).getPrePopData(any())(any())
      verify(mockHipPrePopConnector, times(0)).getHipPrePopData(any())(any())
    }

    "use HIP if feature switch is true" in {
      setup(true)
      val result = controller.prePop(testNino)(FakeRequest())
      status(result) mustBe OK
      contentAsJson(result) mustBe Json.toJson(data)
      verify(mockPrePopConnector, times(0)).getPrePopData(any())(any())
      verify(mockHipPrePopConnector, times(1)).getHipPrePopData(any())(any())
    }
  }
}
