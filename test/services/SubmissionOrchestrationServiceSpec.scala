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

package services

import connectors.RegistrationSuccess
import connectors.mocks.subscription.{MockBusinessConnector, MockPropertyConnector, MockRegistrationConnector}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import services.SubmissionOrchestrationService.SuccessfulSubmission
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import utils.TestConstants._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubmissionOrchestrationServiceSpec extends UnitSpec
  with GuiceOneAppPerSuite
  with MockRegistrationConnector
  with MockBusinessConnector
  with MockPropertyConnector {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  object Service extends SubmissionOrchestrationService(mockRegistrationConnector, mockBusinessConnector, mockPropertyConnector)

  val successfulSubmission = SuccessfulSubmission(mtditId = testMtditId)

  "submit" should {
    "register and subscribe a business for an individual" when {
      "only business income is provided" in {
        mockRegister(testNino, isAnAgent = false)(Future.successful(RegistrationSuccess))
        mockBusinessSubscribe(testNino, testBusinessIncomeSourceModel.businessIncome.get)(Future.successful(testMtditId))

        val result = Service.submit(testBusinessIncomeSourceModel)

        await(result) shouldBe successfulSubmission
      }
    }

    "register and subscribe a property for an individual" when {
      "only property income is provided" in {
        mockRegister(testNino, isAnAgent = false)(Future.successful(RegistrationSuccess))
        mockPropertySubscribe(testNino, testPropertyIncomeSourceModel.propertyIncome.get)(Future.successful(testMtditId))

        val result = Service.submit(testPropertyIncomeSourceModel)

        await(result) shouldBe successfulSubmission
      }
    }

    "register and subscribe a business and a property for an individual" when {
      "both income sources are provided" in {
        mockRegister(testNino, isAnAgent = false)(Future.successful(RegistrationSuccess))
        mockBusinessSubscribe(testNino, testBusinessIncomeSourceModel.businessIncome.get)(Future.successful(testMtditId))
        mockPropertySubscribe(testNino, testPropertyIncomeSourceModel.propertyIncome.get)(Future.successful(testMtditId))

        val result = Service.submit(testBothIncomeSourceModel)

        await(result) shouldBe successfulSubmission
      }
    }

    "register and subscribe a business for an agent" when {
      "only business income is provided" in {
        mockRegister(testNino, isAnAgent = true)(Future.successful(RegistrationSuccess))
        mockBusinessSubscribe(testNino, testBusinessIncomeSourceModel.businessIncome.get)(Future.successful(testMtditId))

        val result = Service.submit(testBusinessIncomeSourceModel)

        await(result) shouldBe successfulSubmission
      }
    }

    "register and subscribe a property for an agent" when {
      "only property income is provided" in {
        mockRegister(testNino, isAnAgent = true)(Future.successful(RegistrationSuccess))
        mockPropertySubscribe(testNino, testPropertyIncomeSourceModel.propertyIncome.get)(Future.successful(testMtditId))

        val result = Service.submit(testPropertyIncomeSourceModel)

        await(result) shouldBe successfulSubmission
      }
    }

    "register and subscribe a business and a property for an agent" when {
      "both income sources are provided" in {
        mockRegister(testNino, isAnAgent = true)(Future.successful(RegistrationSuccess))
        mockBusinessSubscribe(testNino, testBusinessIncomeSourceModel.businessIncome.get)(Future.successful(testMtditId))
        mockPropertySubscribe(testNino, testPropertyIncomeSourceModel.propertyIncome.get)(Future.successful(testMtditId))

        val result = Service.submit(testBothIncomeSourceModel)

        await(result) shouldBe successfulSubmission
      }
    }
  }

}
