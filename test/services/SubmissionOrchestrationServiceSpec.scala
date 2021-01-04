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

package services

import config.AppConfig
import connectors.RegistrationSuccess
import connectors.mocks.subscription.{MockBusinessConnector, MockPropertyConnector, MockRegistrationConnector}
import controllers.ITSASessionKeys
import models.ErrorModel
import models.monitoring.{RegistrationRequestAudit, RegistrationSuccessAudit}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import services.SubmissionOrchestrationService.SuccessfulSubmission
import services.mocks.monitoring.MockAuditService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import utils.TestConstants._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubmissionOrchestrationServiceSpec extends UnitSpec
  with GuiceOneAppPerSuite
  with MockRegistrationConnector
  with MockBusinessConnector
  with MockPropertyConnector
  with MockAuditService {

  val path: String = "testRequestURI"

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val request: Request[AnyContent] = FakeRequest().withHeaders(ITSASessionKeys.RequestURI -> path)

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  object Service extends SubmissionOrchestrationService(
    mockRegistrationConnector,
    mockBusinessConnector,
    mockPropertyConnector,
    mockAuditService,
    appConfig
  )

  val successfulSubmission: SuccessfulSubmission = SuccessfulSubmission(mtditId = testMtditId)

  "submit" should {
    "return the error model from the registration connector" in {
      val errorModel = ErrorModel(Status.INTERNAL_SERVER_ERROR, "test-error")
      mockRegister(testNino, isAnAgent = false)(Future.successful(Left(errorModel)))

      val result = Service.submit(testBusinessIncomeSourceModel)

      await(result) shouldBe Left(errorModel)

      verifyAudit(RegistrationRequestAudit(testBusinessIncomeSourceModel, appConfig.desAuthorisationToken))
    }

    "register and subscribe a business for an individual" when {
      "only business income is provided" in {
        mockRegister(testNino, isAnAgent = false)(Future.successful(Right(RegistrationSuccess)))
        mockBusinessSubscribe(testNino, testBusinessIncomeSourceModel.businessIncome.get, None)(Future.successful(testMtditId))

        val result = Service.submit(testBusinessIncomeSourceModel)

        await(result) shouldBe Right(successfulSubmission)

        verifyAudit(RegistrationRequestAudit(testBusinessIncomeSourceModel, appConfig.desAuthorisationToken))
        verifyAudit(RegistrationSuccessAudit(testBusinessIncomeSourceModel.arn, testBusinessIncomeSourceModel.nino, successfulSubmission.mtditId,
          appConfig.desAuthorisationToken,Some(path)))
      }
    }

    "register and subscribe a property for an individual" when {
      "only property income is provided" in {
        mockRegister(testNino, isAnAgent = false)(Future.successful(Right(RegistrationSuccess)))
        mockPropertySubscribe(testNino, testPropertyIncomeSourceModel.propertyIncome.get, None)(Future.successful(testMtditId))

        val result = Service.submit(testPropertyIncomeSourceModel)

        await(result) shouldBe Right(successfulSubmission)

        verifyAudit(RegistrationRequestAudit(testPropertyIncomeSourceModel, appConfig.desAuthorisationToken))
        verifyAudit(RegistrationSuccessAudit(testPropertyIncomeSourceModel.arn, testPropertyIncomeSourceModel.nino,successfulSubmission.mtditId,
          appConfig.desAuthorisationToken,Some(path)))
      }
    }

    "register and subscribe a business and a property for an individual" when {
      "both income sources are provided" in {
        mockRegister(testNino, isAnAgent = false)(Future.successful(Right(RegistrationSuccess)))
        mockBusinessSubscribe(testNino, testBothIncomeSourceModel.businessIncome.get, None)(Future.successful(testMtditId))
        mockPropertySubscribe(testNino, testBothIncomeSourceModel.propertyIncome.get, None)(Future.successful(testMtditId))

        val result = Service.submit(testBothIncomeSourceModel)

        await(result) shouldBe Right(successfulSubmission)

        verifyAudit(RegistrationRequestAudit(testBothIncomeSourceModel, appConfig.desAuthorisationToken))
        verifyAudit(RegistrationSuccessAudit(testBothIncomeSourceModel.arn, testBothIncomeSourceModel.nino, successfulSubmission.mtditId,
          appConfig.desAuthorisationToken,Some(path)))
      }
    }

    "register and subscribe a business for an agent" when {
      "only business income is provided" in {
        mockRegister(testNino, isAnAgent = true)(Future.successful(Right(RegistrationSuccess)))
        mockBusinessSubscribe(testNino, testBusinessIncomeSourceModel.businessIncome.get, Some(testArn))(Future.successful(testMtditId))

        val result = Service.submit(testBusinessIncomeSourceModel)

        await(result) shouldBe Right(successfulSubmission)

        verifyAudit(RegistrationRequestAudit(testBusinessIncomeSourceModel, appConfig.desAuthorisationToken))
        verifyAudit(RegistrationSuccessAudit(testBusinessIncomeSourceModel.arn, testBothIncomeSourceModel.nino, successfulSubmission.mtditId,
          appConfig.desAuthorisationToken,Some(path)))
      }
    }

    "register and subscribe a property for an agent" when {
      "only property income is provided" in {
        mockRegister(testNino, isAnAgent = true)(Future.successful(Right(RegistrationSuccess)))
        mockPropertySubscribe(testNino, testPropertyIncomeSourceModel.propertyIncome.get, Some(testArn))(Future.successful(testMtditId))

        val result = Service.submit(testPropertyIncomeSourceModel)

        await(result) shouldBe Right(successfulSubmission)

        verifyAudit(RegistrationRequestAudit(testPropertyIncomeSourceModel, appConfig.desAuthorisationToken))
        verifyAudit(RegistrationSuccessAudit(testPropertyIncomeSourceModel.arn, testPropertyIncomeSourceModel.nino, successfulSubmission.mtditId,
          appConfig.desAuthorisationToken,Some(path)))
      }
    }

    "register and subscribe a business and a property for an agent" when {
      "both income sources are provided" in {
        mockRegister(testNino, isAnAgent = true)(Future.successful(Right(RegistrationSuccess)))
        mockBusinessSubscribe(testNino, testBothIncomeSourceModel.businessIncome.get, Some(testArn))(Future.successful(testMtditId))
        mockPropertySubscribe(testNino, testBothIncomeSourceModel.propertyIncome.get, Some(testArn))(Future.successful(testMtditId))

        val result = Service.submit(testBothIncomeSourceModel)

        await(result) shouldBe Right(successfulSubmission)

        verifyAudit(RegistrationRequestAudit(testBothIncomeSourceModel, appConfig.desAuthorisationToken))
        verifyAudit(RegistrationSuccessAudit(testBothIncomeSourceModel.arn, testBothIncomeSourceModel.nino, successfulSubmission.mtditId,
          appConfig.desAuthorisationToken,Some(path)))
      }
    }
  }

}
